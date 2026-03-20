package com.jsamuelsen11.daykeeper.feature.calendar.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar as DayKeeperCalendar
import com.jsamuelsen11.daykeeper.core.model.calendar.EventType
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme
import com.jsamuelsen11.daykeeper.feature.calendar.createedit.EventFormState
import com.jsamuelsen11.daykeeper.feature.calendar.createedit.ReminderEntry

@Preview(showBackground = true)
@Composable
private fun EventFormContentEmptyPreview() {
  DayKeeperTheme {
    EventFormContent(
      state = EventFormState(),
      calendars =
        listOf(
          DayKeeperCalendar(
            calendarId = "c1",
            spaceId = "s1",
            tenantId = "t1",
            name = "Work",
            normalizedName = "work",
            color = "#4CAF50",
            isDefault = true,
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_700_000_000_000L,
          )
        ),
      eventTypes =
        listOf(
          EventType(
            eventTypeId = "et1",
            name = "Meeting",
            normalizedName = "meeting",
            isSystem = true,
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_700_000_000_000L,
          )
        ),
      callbacks = previewCallbacks(),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun EventFormContentWithRemindersPreview() {
  DayKeeperTheme {
    EventFormContent(
      state =
        EventFormState(
          title = "Team Standup",
          calendarId = "c1",
          isAllDay = false,
          startAt = 1_742_380_200_000L,
          endAt = 1_742_383_800_000L,
          location = "Room 3B",
          reminders =
            listOf(
              ReminderEntry(id = "r1", minutesBefore = 15),
              ReminderEntry(id = "r2", minutesBefore = 60),
            ),
        ),
      calendars =
        listOf(
          DayKeeperCalendar(
            calendarId = "c1",
            spaceId = "s1",
            tenantId = "t1",
            name = "Work",
            normalizedName = "work",
            color = "#4CAF50",
            isDefault = true,
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_700_000_000_000L,
          )
        ),
      eventTypes = emptyList(),
      callbacks = previewCallbacks(),
    )
  }
}

private fun previewCallbacks() =
  EventFormCallbacks(
    onTitleChanged = {},
    onDescriptionChanged = {},
    onCalendarSelected = {},
    onEventTypeSelected = {},
    onAllDayToggled = {},
    onStartDateSelected = {},
    onStartTimeSelected = {},
    onEndDateSelected = {},
    onEndTimeSelected = {},
    onLocationChanged = {},
    onRecurrenceChanged = {},
    onAddReminder = {},
    onRemoveReminder = {},
    onSave = {},
  )
