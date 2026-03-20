package com.jsamuelsen11.daykeeper.feature.calendar.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val DEFAULT_SPACE_ID = "default-space"

/**
 * ViewModel for the calendar management screen.
 *
 * Observes all calendars in the default space and computes the non-deleted event count for each
 * calendar reactively.
 *
 * @param calendarRepository Source of truth for calendar data.
 * @param eventRepository Source of truth for event data (used to compute per-calendar event
 *   counts).
 */
class CalendarManagementViewModel(
  private val calendarRepository: CalendarRepository,
  private val eventRepository: EventRepository,
) : ViewModel() {

  /** The reactive UI state for this screen. Starts as [CalendarManagementUiState.Loading]. */
  val uiState: StateFlow<CalendarManagementUiState> =
    combine(
        calendarRepository.observeBySpace(DEFAULT_SPACE_ID),
        eventRepository.observeByCalendar(DEFAULT_SPACE_ID),
      ) { calendars, allEvents ->
        val activeCalendars = calendars.filter { it.deletedAt == null }
        val activeEvents = allEvents.filter { it.deletedAt == null }
        val countByCalendar = activeEvents.groupingBy { it.calendarId }.eachCount()
        val items =
          activeCalendars
            .sortedWith(
              compareByDescending<com.jsamuelsen11.daykeeper.core.model.calendar.Calendar> {
                  it.isDefault
                }
                .thenBy { it.name }
            )
            .map { calendar ->
              CalendarListItem(
                calendar = calendar,
                eventCount = countByCalendar[calendar.calendarId] ?: 0,
              )
            }
        CalendarManagementUiState.Success(items) as CalendarManagementUiState
      }
      .catch { e -> emit(CalendarManagementUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        CalendarManagementUiState.Loading,
      )

  /** Soft-deletes the calendar identified by [calendarId]. */
  fun deleteCalendar(calendarId: String) {
    viewModelScope.launch { calendarRepository.delete(calendarId) }
  }
}
