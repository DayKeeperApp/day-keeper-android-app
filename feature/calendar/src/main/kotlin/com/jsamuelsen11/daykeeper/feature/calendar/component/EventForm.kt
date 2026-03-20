package com.jsamuelsen11.daykeeper.feature.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar as DayKeeperCalendar
import com.jsamuelsen11.daykeeper.core.model.calendar.EventType
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperDatePicker
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperDateTimePicker
import com.jsamuelsen11.daykeeper.core.ui.component.RecurrencePicker
import com.jsamuelsen11.daykeeper.core.ui.component.ReminderConfigurator
import com.jsamuelsen11.daykeeper.feature.calendar.createedit.EventFormState

// region Constants

private val ContentPadding = 16.dp
private val SectionSpacing = 20.dp
private val SectionDividerSpacing = 4.dp

// endregion

// region Dialog visibility state

internal enum class ActiveDialog {
  NONE,
  START_DATE_TIME,
  START_DATE_ONLY,
  END_DATE_TIME,
  END_DATE_ONLY,
  RECURRENCE,
  REMINDER,
}

// endregion

/**
 * Reusable form for creating or editing a calendar event.
 *
 * Provides fields for all [EventFormState] properties and delegates user interactions back to the
 * caller via fine-grained callbacks. Dialogs for date/time pickers, the recurrence picker, and the
 * reminder configurator are managed internally by this composable.
 *
 * @param state Current form state.
 * @param calendars Available calendars for the calendar selector.
 * @param eventTypes Available event types for the event-type selector.
 * @param callbacks Grouped callbacks for all form interactions.
 * @param modifier Modifier applied to the root scrollable [Column].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun EventFormContent(
  state: EventFormState,
  calendars: List<DayKeeperCalendar>,
  eventTypes: List<EventType>,
  callbacks: EventFormCallbacks,
  modifier: Modifier = Modifier,
) {
  var activeDialog by remember { mutableStateOf(ActiveDialog.NONE) }
  val dismiss: () -> Unit = { activeDialog = ActiveDialog.NONE }

  EventFormDialogs(
    activeDialog = activeDialog,
    state = state,
    onDismiss = dismiss,
    onStartDateSelected = {
      callbacks.onStartDateSelected(it)
      dismiss()
    },
    onStartTimeSelected = {
      callbacks.onStartTimeSelected(it)
      dismiss()
    },
    onEndDateSelected = {
      callbacks.onEndDateSelected(it)
      dismiss()
    },
    onEndTimeSelected = {
      callbacks.onEndTimeSelected(it)
      dismiss()
    },
    onRecurrenceChanged = {
      callbacks.onRecurrenceChanged(it)
      dismiss()
    },
    onAddReminder = {
      callbacks.onAddReminder(it)
      dismiss()
    },
  )

  EventFormBody(
    state = state,
    calendars = calendars,
    eventTypes = eventTypes,
    callbacks = callbacks,
    onDialogChange = { activeDialog = it },
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventFormBody(
  state: EventFormState,
  calendars: List<DayKeeperCalendar>,
  eventTypes: List<EventType>,
  callbacks: EventFormCallbacks,
  onDialogChange: (ActiveDialog) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.verticalScroll(rememberScrollState()).padding(ContentPadding),
    verticalArrangement = Arrangement.spacedBy(SectionSpacing),
  ) {
    EventFormTopFields(
      state = state,
      calendars = calendars,
      eventTypes = eventTypes,
      callbacks = callbacks,
      onDialogChange = onDialogChange,
    )
    HorizontalDivider(modifier = Modifier.padding(vertical = SectionDividerSpacing))
    EventFormBottomFields(state = state, callbacks = callbacks, onDialogChange = onDialogChange)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventFormTopFields(
  state: EventFormState,
  calendars: List<DayKeeperCalendar>,
  eventTypes: List<EventType>,
  callbacks: EventFormCallbacks,
  onDialogChange: (ActiveDialog) -> Unit,
) {
  TitleField(
    title = state.title,
    error = state.titleError,
    enabled = !state.isSaving,
    onTitleChanged = callbacks.onTitleChanged,
  )
  DescriptionField(
    description = state.description,
    enabled = !state.isSaving,
    onDescriptionChanged = callbacks.onDescriptionChanged,
  )
  CalendarSelector(
    calendars = calendars,
    selectedCalendarId = state.calendarId,
    enabled = !state.isSaving,
    onCalendarSelected = callbacks.onCalendarSelected,
  )
  EventTypeSelector(
    eventTypes = eventTypes,
    selectedEventTypeId = state.eventTypeId,
    enabled = !state.isSaving,
    onEventTypeSelected = callbacks.onEventTypeSelected,
  )
  AllDayToggleRow(
    isAllDay = state.isAllDay,
    enabled = !state.isSaving,
    onAllDayToggled = callbacks.onAllDayToggled,
  )
  HorizontalDivider(modifier = Modifier.padding(vertical = SectionDividerSpacing))
  if (state.isAllDay) {
    AllDayDateSection(
      startDate = state.startDate,
      endDate = state.endDate,
      dateError = state.dateError,
      enabled = !state.isSaving,
      onPickStartDate = { onDialogChange(ActiveDialog.START_DATE_ONLY) },
      onPickEndDate = { onDialogChange(ActiveDialog.END_DATE_ONLY) },
    )
  } else {
    TimedDateSection(
      startAt = state.startAt,
      endAt = state.endAt,
      timezone = state.timezone,
      dateError = state.dateError,
      enabled = !state.isSaving,
      onPickStartDateTime = { onDialogChange(ActiveDialog.START_DATE_TIME) },
      onPickEndDateTime = { onDialogChange(ActiveDialog.END_DATE_TIME) },
    )
  }
}

@Composable
private fun EventFormBottomFields(
  state: EventFormState,
  callbacks: EventFormCallbacks,
  onDialogChange: (ActiveDialog) -> Unit,
) {
  LocationField(
    location = state.location.orEmpty(),
    enabled = !state.isSaving,
    onLocationChanged = callbacks.onLocationChanged,
  )
  RecurrenceRow(
    recurrenceRule = state.recurrenceRule,
    enabled = !state.isSaving,
    onPickRecurrence = { onDialogChange(ActiveDialog.RECURRENCE) },
  )
  HorizontalDivider(modifier = Modifier.padding(vertical = SectionDividerSpacing))
  RemindersSection(
    reminders = state.reminders,
    enabled = !state.isSaving,
    onAddReminder = { onDialogChange(ActiveDialog.REMINDER) },
    onRemoveReminder = callbacks.onRemoveReminder,
  )
  Spacer(modifier = Modifier.height(SectionSpacing))
  TextButton(
    onClick = callbacks.onSave,
    enabled = !state.isSaving,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text("Save")
  }
}

// region Dialogs

@Composable
private fun EventFormDialogs(
  activeDialog: ActiveDialog,
  state: EventFormState,
  onDismiss: () -> Unit,
  onStartDateSelected: (String) -> Unit,
  onStartTimeSelected: (Long) -> Unit,
  onEndDateSelected: (String) -> Unit,
  onEndTimeSelected: (Long) -> Unit,
  onRecurrenceChanged: (RecurrenceRule?) -> Unit,
  onAddReminder: (Int) -> Unit,
) {
  when (activeDialog) {
    ActiveDialog.START_DATE_TIME -> {
      DayKeeperDateTimePicker(
        onDateTimeSelected = { dateMillis, hour, minute ->
          val combined = combineDateAndTime(dateMillis, hour, minute, state.timezone)
          onStartTimeSelected(combined)
          onStartDateSelected(epochMillisToIsoDate(dateMillis, state.timezone))
        },
        onDismiss = onDismiss,
        initialDateMillis = state.startAt,
      )
    }
    ActiveDialog.START_DATE_ONLY -> {
      DayKeeperDatePicker(
        onDateSelected = { millis ->
          onStartDateSelected(epochMillisToIsoDate(millis, state.timezone))
        },
        onDismiss = onDismiss,
        initialDateMillis = state.startDate?.let { parseIsoDateToMillis(it, state.timezone) },
      )
    }
    ActiveDialog.END_DATE_TIME -> {
      DayKeeperDateTimePicker(
        onDateTimeSelected = { dateMillis, hour, minute ->
          val combined = combineDateAndTime(dateMillis, hour, minute, state.timezone)
          onEndTimeSelected(combined)
          onEndDateSelected(epochMillisToIsoDate(dateMillis, state.timezone))
        },
        onDismiss = onDismiss,
        initialDateMillis = state.endAt,
      )
    }
    ActiveDialog.END_DATE_ONLY -> {
      DayKeeperDatePicker(
        onDateSelected = { millis ->
          onEndDateSelected(epochMillisToIsoDate(millis, state.timezone))
        },
        onDismiss = onDismiss,
        initialDateMillis = state.endDate?.let { parseIsoDateToMillis(it, state.timezone) },
      )
    }
    ActiveDialog.RECURRENCE -> {
      RecurrencePicker(
        onRecurrenceSelected = onRecurrenceChanged,
        onDismiss = onDismiss,
        initialRule = state.recurrenceRule,
      )
    }
    ActiveDialog.REMINDER -> {
      ReminderConfigurator(onReminderSelected = onAddReminder, onDismiss = onDismiss)
    }
    ActiveDialog.NONE -> Unit
  }
}

// endregion
