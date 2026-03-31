package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 5f
private const val TITLE_MAX_LINES = 1
private const val LABEL_CLOSE = "Close"

/**
 * Full-screen image preview dialog with pinch-to-zoom and pan support.
 *
 * @param imageModel The image source — local path or remote URL.
 * @param fileName Display name shown in the top bar.
 * @param onDismiss Called when the user closes the preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreview(
  imageModel: Any?,
  fileName: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    var scale by remember { mutableFloatStateOf(MIN_SCALE) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
      modifier = modifier.fillMaxSize().background(Color.Black),
      topBar = {
        TopAppBar(
          title = {
            Text(text = fileName, maxLines = TITLE_MAX_LINES, overflow = TextOverflow.Ellipsis)
          },
          navigationIcon = {
            IconButton(onClick = onDismiss) {
              Icon(imageVector = DayKeeperIcons.Close, contentDescription = LABEL_CLOSE)
            }
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = Color.Black.copy(alpha = 0.6f),
              titleContentColor = Color.White,
              navigationIconContentColor = Color.White,
            ),
        )
      },
      containerColor = Color.Black,
    ) { padding ->
      Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        AsyncImage(
          model = imageModel,
          contentDescription = fileName,
          contentScale = ContentScale.Fit,
          modifier =
            Modifier.fillMaxSize()
              .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y,
              )
              .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                  scale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                  offset =
                    if (scale > MIN_SCALE) {
                      Offset(offset.x + pan.x, offset.y + pan.y)
                    } else {
                      Offset.Zero
                    }
                }
              },
        )
      }
    }
  }
}
