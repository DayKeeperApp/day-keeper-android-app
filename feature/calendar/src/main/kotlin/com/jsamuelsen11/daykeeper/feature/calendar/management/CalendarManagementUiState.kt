package com.jsamuelsen11.daykeeper.feature.calendar.management

import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar

/** UI state for the calendar management (list) screen. */
sealed interface CalendarManagementUiState {
  /** Initial state while calendars are loading. */
  data object Loading : CalendarManagementUiState

  /**
   * Successfully loaded state.
   *
   * @property items The list of calendars with their event counts, sorted by name.
   */
  data class Success(val items: List<CalendarListItem>) : CalendarManagementUiState

  /**
   * Error state when calendars cannot be loaded.
   *
   * @property message A human-readable description of the error.
   */
  data class Error(val message: String) : CalendarManagementUiState
}

/**
 * A calendar enriched with its total non-deleted event count.
 *
 * @property calendar The underlying calendar domain model.
 * @property eventCount Total number of non-deleted events belonging to this calendar.
 */
data class CalendarListItem(val calendar: Calendar, val eventCount: Int)
