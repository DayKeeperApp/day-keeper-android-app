package com.jsamuelsen11.daykeeper.feature.calendar.detail

import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import com.jsamuelsen11.daykeeper.core.model.calendar.EventType

/** UI state for the event detail screen. */
sealed interface EventDetailUiState {
  /** Initial state while event data is loading. */
  data object Loading : EventDetailUiState

  /**
   * Successfully loaded state containing the event and its related entities.
   *
   * @property event The event being displayed.
   * @property calendar The calendar this event belongs to, or null if it cannot be resolved.
   * @property eventType The event type for this event, or null if no type is assigned.
   * @property reminders All non-deleted reminders associated with this event.
   * @property attachments Attachments associated with this event.
   */
  data class Success(
    val event: Event,
    val calendar: Calendar?,
    val eventType: EventType?,
    val reminders: List<EventReminder>,
    val attachments: List<AttachmentUiItem> = emptyList(),
  ) : EventDetailUiState

  /**
   * Error state when the event cannot be loaded.
   *
   * @property message A human-readable description of the error.
   */
  data class Error(val message: String) : EventDetailUiState
}
