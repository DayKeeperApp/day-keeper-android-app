package com.jsamuelsen11.daykeeper.feature.calendar.createedit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.feature.calendar.component.EventFormCallbacks
import com.jsamuelsen11.daykeeper.feature.calendar.component.EventFormContent
import org.koin.compose.viewmodel.koinViewModel

private const val LABEL_NEW_EVENT = "New Event"
private const val LABEL_EDIT_EVENT = "Edit Event"

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
          modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
    }
  }
}
