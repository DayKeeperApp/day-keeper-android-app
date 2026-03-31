package com.jsamuelsen11.daykeeper.feature.calendar.createedit

import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar
import com.jsamuelsen11.daykeeper.core.model.calendar.EventType
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import java.util.TimeZone

/** UI state for the event create/edit screen. */
sealed interface EventCreateEditUiState {
  /** Initial state while an existing event or default values are loading. */
  data object Loading : EventCreateEditUiState

  /**
   * Form is ready for interaction.
   *
   * @property formState The current values of all form fields.
   * @property isEditing Whether the screen is editing an existing event.
   * @property calendars Available calendars the user can assign the event to.
   * @property eventTypes Available event types the user can assign to the event.
   * @property attachments Attachments associated with this event when editing.
   */
  data class Ready(
    val formState: EventFormState,
    val isEditing: Boolean = false,
    val calendars: List<Calendar> = emptyList(),
    val eventTypes: List<EventType> = emptyList(),
    val attachments: List<AttachmentUiItem> = emptyList(),
  ) : EventCreateEditUiState
}

/** One-shot events emitted by [EventCreateEditViewModel]. */
sealed interface EventCreateEditEvent {
  /** Emitted after the event has been successfully saved. */
  data object Saved : EventCreateEditEvent
}

/**
 * Holds the mutable form fields for the event create/edit screen.
 *
 * @property title Current value of the title field.
 * @property description Current value of the optional description field.
 * @property calendarId ID of the selected calendar, or null when none is selected.
 * @property isAllDay Whether this is an all-day event.
 * @property startDate ISO-8601 date string for all-day start, or null.
 * @property endDate ISO-8601 date string for all-day end, or null.
 * @property startAt Epoch millis for timed event start, or null.
 * @property endAt Epoch millis for timed event end, or null.
 * @property timezone IANA time zone ID (e.g. `"America/New_York"`).
 * @property eventTypeId ID of the selected event type, or null.
 * @property location Optional location string.
 * @property recurrenceRule Recurrence rule for repeating events, or null.
 * @property reminders List of reminder entries to be saved with the event.
 * @property titleError Validation error for the title field, or null if valid.
 * @property dateError Validation error for date/time fields, or null if valid.
 * @property isSaving Whether a save operation is currently in progress.
 */
data class EventFormState(
  val title: String = "",
  val description: String = "",
  val calendarId: String? = null,
  val isAllDay: Boolean = false,
  val startDate: String? = null,
  val endDate: String? = null,
  val startAt: Long? = null,
  val endAt: Long? = null,
  val timezone: String = TimeZone.getDefault().id,
  val eventTypeId: String? = null,
  val location: String? = null,
  val recurrenceRule: RecurrenceRule? = null,
  val reminders: List<ReminderEntry> = emptyList(),
  val titleError: String? = null,
  val dateError: String? = null,
  val isSaving: Boolean = false,
)

/**
 * A lightweight representation of a single reminder pending save.
 *
 * @property id A locally-generated UUID used as a stable key before the entity is persisted.
 * @property minutesBefore Number of minutes before the event start to trigger the reminder.
 */
data class ReminderEntry(val id: String, val minutesBefore: Int)
