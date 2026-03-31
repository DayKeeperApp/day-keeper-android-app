package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.model.attachment.DownloadState
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons

private val CardSize = 80.dp
private val CardSpacing = 8.dp
private val RowContentPadding = 16.dp
private val ProgressStroke = 2.dp
private val ProgressSize = 24.dp
private const val FILE_NAME_MAX_LINES = 2
private val ThumbnailPadding = 4.dp

private const val LABEL_DELETE = "Delete"
private const val LABEL_ADD_ATTACHMENT = "Add attachment"
private const val PDF_MIME_TYPE = "application/pdf"

/**
 * Horizontal scrollable row of attachment thumbnails with an add button at the end.
 *
 * Images display an [AsyncImage] preview. PDFs show an icon with the filename. Each card shows a
 * download progress indicator when downloading. Long-press opens a delete menu.
 */
@Composable
fun AttachmentRow(
  attachments: List<AttachmentUiItem>,
  onAddClick: () -> Unit,
  onAttachmentClick: (AttachmentUiItem) -> Unit,
  onDeleteAttachment: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyRow(
    modifier = modifier,
    contentPadding = PaddingValues(horizontal = RowContentPadding),
    horizontalArrangement = Arrangement.spacedBy(CardSpacing),
  ) {
    items(attachments, key = { it.attachmentId }) { item ->
      AttachmentThumbnailCard(
        item = item,
        onClick = { onAttachmentClick(item) },
        onDelete = { onDeleteAttachment(item.attachmentId) },
      )
    }
    item(key = "add_button") { AddAttachmentCard(onClick = onAddClick) }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AttachmentThumbnailCard(
  item: AttachmentUiItem,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }

  Box {
    Card(
      modifier =
        Modifier.size(CardSize)
          .combinedClickable(onClick = onClick, onLongClick = { showMenu = true }),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        if (item.mimeType == PDF_MIME_TYPE) {
          PdfThumbnailContent(fileName = item.fileName)
        } else {
          ImageThumbnailContent(item = item)
        }
        DownloadOverlay(downloadState = item.downloadState)
      }
    }
    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
      DropdownMenuItem(
        text = { Text(LABEL_DELETE) },
        onClick = {
          showMenu = false
          onDelete()
        },
        leadingIcon = { Icon(DayKeeperIcons.Delete, contentDescription = LABEL_DELETE) },
      )
    }
  }
}

@Composable
private fun ImageThumbnailContent(item: AttachmentUiItem) {
  val model = item.localPath ?: item.remoteUrl
  AsyncImage(
    model = model,
    contentDescription = item.fileName,
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop,
  )
}

@Composable
private fun PdfThumbnailContent(fileName: String) {
  Column(
    modifier = Modifier.fillMaxSize().padding(ThumbnailPadding),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = DayKeeperIcons.PdfDocument,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = fileName,
      style = MaterialTheme.typography.labelSmall,
      maxLines = FILE_NAME_MAX_LINES,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun DownloadOverlay(downloadState: DownloadState) {
  when (downloadState) {
    is DownloadState.NotDownloaded -> {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
          imageVector = DayKeeperIcons.Download,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
      }
    }
    is DownloadState.Downloading -> {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
          progress = { downloadState.progress },
          modifier = Modifier.size(ProgressSize),
          strokeWidth = ProgressStroke,
        )
      }
    }
    is DownloadState.Downloaded,
    is DownloadState.Failed -> {
      /* no overlay */
    }
  }
}

@Composable
private fun AddAttachmentCard(onClick: () -> Unit) {
  OutlinedCard(
    modifier = Modifier.size(CardSize),
    onClick = onClick,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Icon(
        imageVector = DayKeeperIcons.Add,
        contentDescription = LABEL_ADD_ATTACHMENT,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
