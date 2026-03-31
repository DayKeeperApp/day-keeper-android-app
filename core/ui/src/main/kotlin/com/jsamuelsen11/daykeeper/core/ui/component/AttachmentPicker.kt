package com.jsamuelsen11.daykeeper.core.ui.component

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import java.io.File

private val RowPadding = 16.dp
private val IconTextSpacing = 16.dp
private val SheetBottomPadding = 32.dp
private const val CAMERA_PHOTO_DIR = "camera_photos"
private const val CAMERA_PHOTO_PREFIX = "photo_"
private const val CAMERA_PHOTO_SUFFIX = ".jpg"

private val DOCUMENT_MIME_TYPES =
  arrayOf("image/jpeg", "image/png", "image/webp", "application/pdf")

/**
 * Bottom sheet picker for selecting attachment sources: camera, gallery, or file browser.
 *
 * @param onDismiss Called when the sheet is dismissed without selection.
 * @param onImageCaptured Called with the URI of a photo captured by the camera.
 * @param onFileSelected Called with the URI of a file selected from gallery or file browser.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPicker(
  onDismiss: () -> Unit,
  onImageCaptured: (Uri) -> Unit,
  onFileSelected: (Uri) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState()

  var cameraUri by remember { mutableStateOf<Uri?>(null) }

  val cameraLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
      if (success) {
        cameraUri?.let { onImageCaptured(it) }
      }
      onDismiss()
    }

  val cameraPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted) {
        val photoFile =
          File(context.cacheDir, CAMERA_PHOTO_DIR)
            .apply { mkdirs() }
            .let { dir -> File.createTempFile(CAMERA_PHOTO_PREFIX, CAMERA_PHOTO_SUFFIX, dir) }
        val uri =
          FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
        cameraUri = uri
        cameraLauncher.launch(uri)
      } else {
        onDismiss()
      }
    }

  val galleryLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) onFileSelected(uri) else onDismiss()
    }

  val documentLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) onFileSelected(uri) else onDismiss()
    }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, modifier = modifier) {
    Column(modifier = Modifier.padding(bottom = SheetBottomPadding)) {
      PickerOption(
        icon = DayKeeperIcons.Camera,
        label = "Take Photo",
        onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
      )
      PickerOption(
        icon = DayKeeperIcons.Gallery,
        label = "Choose from Gallery",
        onClick = {
          galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
          )
        },
      )
      PickerOption(
        icon = DayKeeperIcons.Attachment,
        label = "Choose File",
        onClick = { documentLauncher.launch(DOCUMENT_MIME_TYPES) },
      )
    }
  }
}

@Composable
private fun PickerOption(icon: ImageVector, label: String, onClick: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(RowPadding),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.width(IconTextSpacing))
    Text(text = label, style = MaterialTheme.typography.bodyLarge)
  }
}
