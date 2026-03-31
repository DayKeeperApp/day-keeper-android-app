package com.jsamuelsen11.daykeeper.feature.calendar.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.ui.component.AttachmentRow
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.component.ImagePreview
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.calendar.component.parseHexColor
import org.koin.compose.viewmodel.koinViewModel

private val ContentPadding = 16.dp
private val SectionSpacing = 12.dp
private val MetaRowSpacing = 8.dp
private val MetaIconTextSpacing = 8.dp
private val ColorBarHeight = 8.dp
private const val LABEL_EVENT = "Event"
private const val LABEL_EDIT = "Edit"
private const val LABEL_DELETE = "Delete"
private const val LABEL_MORE_OPTIONS = "More options"
private const val LABEL_DELETE_CONFIRM_TITLE = "Delete Event"
private const val LABEL_DELETE_CONFIRM_MESSAGE = "Are you sure you want to delete this event?"
private const val LABEL_REMINDERS = "Reminders"
private const val LABEL_ATTACHMENTS = "Attachments"
private const val MIME_TYPE_IMAGE_PREFIX = "image/"

@Composable
fun EventDetailScreen(
  onNavigateBack: () -> Unit,
  onEditEvent: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: EventDetailViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showDeleteDialog by remember { mutableStateOf(false) }

  if (showDeleteDialog) {
    ConfirmationDialog(
      title = LABEL_DELETE_CONFIRM_TITLE,
      body = LABEL_DELETE_CONFIRM_MESSAGE,
      onConfirm = {
        showDeleteDialog = false
        viewModel.deleteEvent()
        onNavigateBack()
      },
      onDismiss = { showDeleteDialog = false },
    )
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      EventDetailTopBar(
        onNavigateBack = onNavigateBack,
        onEditEvent = onEditEvent,
        onDeleteEvent = { showDeleteDialog = true },
      )
    },
  ) { innerPadding ->
    when (val state = uiState) {
      is EventDetailUiState.Loading ->
        LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
      is EventDetailUiState.Error ->
        EmptyStateView(
          icon = DayKeeperIcons.Event,
          title = "Event not found",
          body = state.message,
          modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
      is EventDetailUiState.Success ->
        EventDetailContent(
          state = state,
          viewModel = viewModel,
          modifier = Modifier.padding(innerPadding),
        )
    }
  }
}

@Composable
private fun EventDetailTopBar(
  onNavigateBack: () -> Unit,
  onEditEvent: () -> Unit,
  onDeleteEvent: () -> Unit,
) {
  var menuExpanded by remember { mutableStateOf(false) }
  DayKeeperTopAppBar(
    title = LABEL_EVENT,
    onNavigationClick = onNavigateBack,
    actions = {
      IconButton(onClick = onEditEvent) {
        Icon(imageVector = DayKeeperIcons.Edit, contentDescription = LABEL_EDIT)
      }
      IconButton(onClick = { menuExpanded = true }) {
        Icon(imageVector = DayKeeperIcons.MoreVert, contentDescription = LABEL_MORE_OPTIONS)
      }
      DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
        DropdownMenuItem(
          text = { Text(LABEL_DELETE) },
          onClick = {
            menuExpanded = false
            onDeleteEvent()
          },
          leadingIcon = { Icon(imageVector = DayKeeperIcons.Delete, contentDescription = null) },
        )
      }
    },
  )
}

@Composable
private fun EventDetailContent(
  state: EventDetailUiState.Success,
  viewModel: EventDetailViewModel,
  modifier: Modifier = Modifier,
) {
  var previewAttachment by remember { mutableStateOf<AttachmentUiItem?>(null) }

  val currentPreview = previewAttachment
  if (currentPreview != null) {
    ImagePreview(
      imageModel = currentPreview.localPath ?: currentPreview.remoteUrl,
      fileName = currentPreview.fileName,
      onDismiss = { previewAttachment = null },
    )
  }

  Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    val calendarColor = state.calendar?.color?.let(::parseHexColor)
    if (calendarColor != null) {
      Box(modifier = Modifier.fillMaxWidth().height(ColorBarHeight).background(calendarColor))
    }

    Column(
      modifier = Modifier.padding(ContentPadding),
      verticalArrangement = Arrangement.spacedBy(SectionSpacing),
    ) {
      EventMetadataSection(state = state)

      AttachmentSection(
        attachments = state.attachments,
        onPreview = { previewAttachment = it },
        onDownload = viewModel::downloadAttachment,
        onDelete = viewModel::deleteAttachment,
      )
    }
  }
}

@Composable
private fun EventMetadataSection(state: EventDetailUiState.Success) {
  Text(text = state.event.title, style = MaterialTheme.typography.headlineMedium)

  if (state.eventType != null) {
    AssistChip(onClick = {}, label = { Text(state.eventType.name) })
  }

  if (state.calendar != null) {
    MetaRow(
      icon = { Icon(imageVector = DayKeeperIcons.Calendar, contentDescription = null) },
      text = state.calendar.name,
    )
  }

  MetaRow(
    icon = { Icon(imageVector = DayKeeperIcons.Schedule, contentDescription = null) },
    text = state.event.formatDateTimeRange(),
  )

  val recurrenceSummary = state.event.formatRecurrenceSummary()
  if (recurrenceSummary != null) {
    MetaRow(
      icon = { Icon(imageVector = DayKeeperIcons.Repeat, contentDescription = null) },
      text = recurrenceSummary,
    )
  }

  val location = state.event.location
  if (!location.isNullOrBlank()) {
    MetaRow(
      icon = { Icon(imageVector = DayKeeperIcons.Location, contentDescription = null) },
      text = location,
    )
  }

  val description = state.event.description
  if (!description.isNullOrBlank()) {
    Spacer(modifier = Modifier.height(MetaRowSpacing))
    Text(text = description, style = MaterialTheme.typography.bodyLarge)
  }

  if (state.reminders.isNotEmpty()) {
    Spacer(modifier = Modifier.height(MetaRowSpacing))
    Text(text = LABEL_REMINDERS, style = MaterialTheme.typography.titleSmall)
    for (reminder in state.reminders) {
      MetaRow(
        icon = { Icon(imageVector = DayKeeperIcons.Notification, contentDescription = null) },
        text = reminder.formatDisplay(),
      )
    }
  }
}

@Composable
private fun AttachmentSection(
  attachments: List<AttachmentUiItem>,
  onPreview: (AttachmentUiItem) -> Unit,
  onDownload: (AttachmentUiItem) -> Unit,
  onDelete: (String) -> Unit,
) {
  Spacer(modifier = Modifier.height(MetaRowSpacing))
  Text(text = LABEL_ATTACHMENTS, style = MaterialTheme.typography.titleSmall)
  AttachmentRow(
    attachments = attachments,
    onAddClick = {},
    onAttachmentClick = { item ->
      if (item.mimeType.startsWith(MIME_TYPE_IMAGE_PREFIX)) onPreview(item) else onDownload(item)
    },
    onDeleteAttachment = onDelete,
  )
}

@Composable
private fun MetaRow(icon: @Composable () -> Unit, text: String, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(MetaIconTextSpacing),
  ) {
    icon()
    Text(text = text, style = MaterialTheme.typography.bodyMedium)
  }
}
