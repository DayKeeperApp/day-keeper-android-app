package com.jsamuelsen11.daykeeper.feature.calendar.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventTypeRepository
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatus
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatusProvider
import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventType
import com.jsamuelsen11.daykeeper.feature.calendar.component.CalendarEventItem
import com.jsamuelsen11.daykeeper.feature.calendar.component.DayCellData
import com.jsamuelsen11.daykeeper.feature.calendar.component.MAX_EVENT_DOTS
import com.jsamuelsen11.daykeeper.feature.calendar.component.MonthGridData
import com.jsamuelsen11.daykeeper.feature.calendar.component.parseHexColor
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val DEFAULT_SPACE_ID = "default-space"

/**
 * ViewModel for the calendar home screen.
 *
 * Manages view mode, month navigation, date selection, and per-calendar visibility. The reactive
 * pipeline merges timed and all-day events from all visible calendars into [MonthGridData] and
 * derives the event list for the currently selected day.
 *
 * @param eventRepository Source of truth for event data.
 * @param calendarRepository Source of truth for calendar data.
 * @param eventTypeRepository Source of truth for event type data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
  private val eventRepository: EventRepository,
  private val calendarRepository: CalendarRepository,
  private val eventTypeRepository: EventTypeRepository,
  private val syncStatusProvider: SyncStatusProvider,
) : ViewModel() {

  private val currentMonth = MutableStateFlow(YearMonth.now())
  private val selectedDate = MutableStateFlow(LocalDate.now())
  private val viewMode = MutableStateFlow(CalendarViewMode.MONTH)
  private val hiddenCalendarIds = MutableStateFlow<Set<String>>(emptySet())

  /** The reactive UI state for the calendar home screen. Starts as [CalendarUiState.Loading]. */
  val uiState: StateFlow<CalendarUiState> =
    combine(
        calendarRepository.observeBySpace(DEFAULT_SPACE_ID),
        eventTypeRepository.observeAll(),
      ) { calendars, eventTypes ->
        Pair(calendars.filter { it.deletedAt == null }, eventTypes)
      }
      .flatMapLatest { (calendars, eventTypes) ->
        if (calendars.isEmpty()) {
          combine(currentMonth, selectedDate, viewMode, hiddenCalendarIds) {
            month,
            date,
            mode,
            hidden ->
            buildSuccessState(
              month = month,
              selected = date,
              mode = mode,
              hidden = hidden,
              calendars = calendars,
              eventsByDate = emptyMap(),
            )
          }
        } else {
          buildEventsPipeline(calendars, eventTypes)
        }
      }
      .combine(syncStatusProvider.syncStatus) { state, syncStatus ->
        if (state is CalendarUiState.Success) {
          state.copy(isRefreshing = syncStatus is SyncStatus.Syncing)
        } else {
          state
        }
      }
      .catch { e -> emit(CalendarUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        CalendarUiState.Loading,
      )

  fun onRefresh() {
    syncStatusProvider.requestSync()
  }

  /** Advances or rewinds the displayed month by [delta] months (negative = backwards). */
  fun navigateMonth(delta: Int) {
    currentMonth.value = currentMonth.value.plusMonths(delta.toLong())
  }

  /**
   * Selects [date] as the active day. If [date] belongs to a different month than the one currently
   * displayed, the view automatically shifts to that month.
   */
  fun selectDate(date: LocalDate) {
    selectedDate.value = date
    val dateMonth = YearMonth.from(date)
    if (dateMonth != currentMonth.value) {
      currentMonth.value = dateMonth
    }
  }

  /** Switches the calendar display granularity (month, week, or day). */
  fun setViewMode(mode: CalendarViewMode) {
    viewMode.value = mode
  }

  /**
   * Toggles the visibility of the calendar identified by [calendarId]. Hidden calendars are
   * excluded from the grid dots and the event list.
   */
  fun toggleCalendarVisibility(calendarId: String) {
    hiddenCalendarIds.value =
      hiddenCalendarIds.value.toMutableSet().also { set ->
        if (calendarId in set) set.remove(calendarId) else set.add(calendarId)
      }
  }

  /** Resets the selected date and displayed month to today. */
  fun goToToday() {
    val today = LocalDate.now()
    selectedDate.value = today
    currentMonth.value = YearMonth.now()
  }

  // region Private helpers

  private fun buildEventsPipeline(calendars: List<Calendar>, eventTypes: List<EventType>) =
    combine(currentMonth, hiddenCalendarIds) { month, hidden -> Pair(month, hidden) }
      .flatMapLatest { (month, hidden) ->
        val visibleCalendars = calendars.filter { it.calendarId !in hidden }
        if (visibleCalendars.isEmpty()) {
          combine(selectedDate, viewMode) { date, mode ->
            buildSuccessState(
              month = month,
              selected = date,
              mode = mode,
              hidden = hidden,
              calendars = calendars,
              eventsByDate = emptyMap(),
            )
          }
        } else {
          val range = month.visibleDateRange()
          val eventFlows = visibleCalendars.map { calendarEventFlow(it, range) }
          val mergedFlow = mergeCalendarFlows(eventFlows)
          mergedFlow.flatMapLatest { triples ->
            val eventsByDate =
              buildEventsByDate(
                triples,
                eventTypes.associateBy { it.eventTypeId },
                ZoneId.systemDefault(),
              )
            combine(selectedDate, viewMode) { date, mode ->
              buildSuccessState(
                month = month,
                selected = date,
                mode = mode,
                hidden = hidden,
                calendars = calendars,
                eventsByDate = eventsByDate,
              )
            }
          }
        }
      }

  private fun calendarEventFlow(calendar: Calendar, range: ClosedRange<LocalDate>) = run {
    val startMillis =
      range.start.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * MILLIS_PER_SECOND
    val endMillis =
      range.endInclusive.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() *
        MILLIS_PER_SECOND
    combine(
      eventRepository.observeByCalendarAndRange(calendar.calendarId, startMillis, endMillis),
      eventRepository.observeAllDayByCalendarAndRange(
        calendar.calendarId,
        range.start.toString(),
        range.endInclusive.toString(),
      ),
    ) { timed, allDay ->
      Triple(
        calendar,
        timed.filter { it.deletedAt == null },
        allDay.filter { it.deletedAt == null },
      )
    }
  }

  private fun mergeCalendarFlows(
    eventFlows: List<Flow<Triple<Calendar, List<Event>, List<Event>>>>
  ) =
    if (eventFlows.size == 1) {
      eventFlows.first().map { listOf(it) }
    } else {
      combine(eventFlows) { it.toList() }
    }

  private fun buildEventsByDate(
    calendarEventTriples: List<Triple<Calendar, List<Event>, List<Event>>>,
    eventTypeById: Map<String, EventType>,
    zoneId: ZoneId,
  ): Map<LocalDate, List<CalendarEventItem>> {
    val result = mutableMapOf<LocalDate, MutableList<CalendarEventItem>>()
    for ((calendar, timedEvents, allDayEvents) in calendarEventTriples) {
      for (event in timedEvents) {
        val date = event.toLocalDate(zoneId) ?: continue
        val item = event.toCalendarEventItem(calendar, eventTypeById)
        result.getOrPut(date) { mutableListOf() }.add(item)
      }
      for (event in allDayEvents) {
        val dates = event.toLocalDates(zoneId)
        val item = event.toCalendarEventItem(calendar, eventTypeById)
        for (date in dates) {
          result.getOrPut(date) { mutableListOf() }.add(item)
        }
      }
    }
    return result
  }

  private fun buildSuccessState(
    month: YearMonth,
    selected: LocalDate,
    mode: CalendarViewMode,
    hidden: Set<String>,
    calendars: List<Calendar>,
    eventsByDate: Map<LocalDate, List<CalendarEventItem>>,
  ): CalendarUiState {
    val today = LocalDate.now()
    val eventCountByCalendar = mutableMapOf<String, Int>()

    for ((_, items) in eventsByDate) {
      for (item in items) {
        val id = item.event.calendarId
        eventCountByCalendar[id] = (eventCountByCalendar[id] ?: 0) + 1
      }
    }

    val weeks = month.buildMonthGridWeeks()
    val gridWeeks =
      weeks.map { week ->
        week.map { date ->
          val dots =
            (eventsByDate[date] ?: emptyList()).take(MAX_EVENT_DOTS).mapNotNull { item ->
              parseHexColor(item.calendarColor)
            }
          DayCellData(
            date = date,
            isCurrentMonth = YearMonth.from(date) == month,
            isToday = date == today,
            eventDots = dots,
          )
        }
      }

    val calendarToggles =
      calendars
        .sortedBy { it.name }
        .map { calendar ->
          CalendarToggle(
            calendar = calendar,
            isVisible = calendar.calendarId !in hidden,
            eventCount = eventCountByCalendar[calendar.calendarId] ?: 0,
          )
        }

    val selectedDateEvents =
      (eventsByDate[selected] ?: emptyList()).sortedWith(
        compareBy<CalendarEventItem> { it.event.isAllDay.not() }
          .thenBy { it.event.startAt ?: Long.MAX_VALUE }
          .thenBy { it.event.startDate }
          .thenBy { it.event.title }
      )

    return CalendarUiState.Success(
      viewMode = mode,
      currentMonth = month,
      selectedDate = selected,
      monthGrid = MonthGridData(yearMonth = month, weeks = gridWeeks),
      selectedDateEvents = selectedDateEvents,
      calendars = calendarToggles,
    )
  }

  private fun Event.toCalendarEventItem(
    calendar: Calendar,
    eventTypeById: Map<String, EventType>,
  ): CalendarEventItem =
    CalendarEventItem(
      event = this,
      calendarName = calendar.name,
      calendarColor = calendar.color,
      eventTypeName = eventTypeId?.let { eventTypeById[it]?.name },
    )

  // endregion

  companion object {
    private const val MILLIS_PER_SECOND = 1_000L
  }
}
