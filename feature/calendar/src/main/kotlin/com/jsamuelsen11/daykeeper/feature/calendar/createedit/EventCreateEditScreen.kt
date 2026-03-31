package com.jsamuelsen11.daykeeper.feature.calendar.createedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.ui.component.AttachmentPicker
import com.jsamuelsen11.daykeeper.core.ui.component.AttachmentRow
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.ImagePreview
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.feature.calendar.component.EventFormCallbacks
import com.jsamuelsen11.daykeeper.feature.calendar.component.EventFormContent
import org.koin.compose.viewmodel.koinViewModel

private const val LABEL_NEW_EVENT = "New Event"
private const val LABEL_EDIT_EVENT = "Edit Event"
private const val MIME_TYPE_IMAGE_PREFIX = "image/"

@Composable
fun EventCreateEditScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: EventCreateEditViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
      when (event) {
        EventCreateEditEvent.Saved -> onNavigateBack()
      }
    }
  }

  val title =
    when (val state = uiState) {
      is EventCreateEditUiState.Ready -> if (state.isEditing) LABEL_EDIT_EVENT else LABEL_NEW_EVENT
      else -> LABEL_NEW_EVENT
    }

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = title, onNavigationClick = onNavigateBack) },
  ) { innerPadding ->
    when (val state = uiState) {
      is EventCreateEditUiState.Loading ->
        LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
      is EventCreateEditUiState.Ready ->
        EventCreateEditContent(
          state = state,
          viewModel = viewModel,
          modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
    }
  }
}

@Composable
private fun EventCreateEditContent(
  state: EventCreateEditUiState.Ready,
  viewModel: EventCreateEditViewModel,
  modifier: Modifier = Modifier,
) {
  var showAttachmentPicker by remember { mutableStateOf(false) }
  var previewAttachment by remember { mutableStateOf<AttachmentUiItem?>(null) }

  val currentPreview = previewAttachment
  if (currentPreview != null) {
    ImagePreview(
      imageModel = currentPreview.localPath ?: currentPreview.remoteUrl,
      fileName = currentPreview.fileName,
      onDismiss = { previewAttachment = null },
    )
  }

  if (showAttachmentPicker) {
    AttachmentPicker(
      onDismiss = { showAttachmentPicker = false },
      onImageCaptured = { showAttachmentPicker = false },
      onFileSelected = { showAttachmentPicker = false },
    )
  }

  Column(modifier = modifier) {
    EventFormContent(
      state = state.formState,
      calendars = state.calendars,
      eventTypes = state.eventTypes,
      callbacks =
        EventFormCallbacks(
          onTitleChanged = viewModel::onTitleChanged,
          onDescriptionChanged = viewModel::onDescriptionChanged,
          onCalendarSelected = viewModel::onCalendarSelected,
          onEventTypeSelected = viewModel::onEventTypeSelected,
          onAllDayToggled = viewModel::onAllDayToggled,
          onStartDateSelected = viewModel::onStartDateSelected,
          onStartTimeSelected = viewModel::onStartTimeSelected,
          onEndDateSelected = viewModel::onEndDateSelected,
          onEndTimeSelected = viewModel::onEndTimeSelected,
          onLocationChanged = viewModel::onLocationChanged,
          onRecurrenceChanged = viewModel::onRecurrenceChanged,
          onAddReminder = viewModel::onAddReminder,
          onRemoveReminder = viewModel::onRemoveReminder,
          onSave = viewModel::onSave,
        ),
      modifier = Modifier.weight(1f).fillMaxWidth(),
    )
    AttachmentRow(
      attachments = state.attachments,
      onAddClick = { showAttachmentPicker = true },
      onAttachmentClick = { item ->
        if (item.mimeType.startsWith(MIME_TYPE_IMAGE_PREFIX)) {
          previewAttachment = item
        } else {
          viewModel.downloadAttachment(item)
        }
      },
      onDeleteAttachment = viewModel::deleteAttachment,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
