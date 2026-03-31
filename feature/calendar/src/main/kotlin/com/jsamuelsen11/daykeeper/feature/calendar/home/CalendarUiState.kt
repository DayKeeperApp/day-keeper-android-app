package com.jsamuelsen11.daykeeper.feature.calendar.home

import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar
import com.jsamuelsen11.daykeeper.feature.calendar.component.CalendarEventItem
import com.jsamuelsen11.daykeeper.feature.calendar.component.MonthGridData
import java.time.LocalDate
import java.time.YearMonth

/** UI state for the main calendar home screen. */
sealed interface CalendarUiState {
  /** Initial state while calendars and events are loading. */
  data object Loading : CalendarUiState

  /**
   * Successfully loaded state.
   *
   * @property viewMode The active calendar view (month, week, or day).
   * @property currentMonth The month currently being displayed in the grid.
   * @property selectedDate The day that is highlighted and whose events are listed.
   * @property monthGrid Pre-computed grid data for rendering the month view.
   * @property selectedDateEvents Events occurring on [selectedDate], enriched with display
   *   metadata.
   * @property calendars All calendars in the space with their visibility toggles.
   */
  data class Success(
    val viewMode: CalendarViewMode,
    val currentMonth: YearMonth,
    val selectedDate: LocalDate,
    val monthGrid: MonthGridData,
    val selectedDateEvents: List<CalendarEventItem>,
    val calendars: List<CalendarToggle>,
    val isRefreshing: Boolean = false,
  ) : CalendarUiState

  /**
   * Error state when data cannot be loaded.
   *
   * @property message A human-readable description of the error.
   */
  data class Error(val message: String) : CalendarUiState
}

/** The granularity at which the calendar home screen displays events. */
enum class CalendarViewMode {
  MONTH,
  WEEK,
  DAY,
}

/**
 * A calendar paired with its current visibility state and event count.
 *
 * @property calendar The underlying calendar domain model.
 * @property isVisible Whether events from this calendar are shown in the grid and list.
 * @property eventCount Total number of non-deleted events belonging to this calendar.
 */
data class CalendarToggle(val calendar: Calendar, val isVisible: Boolean, val eventCount: Int)
