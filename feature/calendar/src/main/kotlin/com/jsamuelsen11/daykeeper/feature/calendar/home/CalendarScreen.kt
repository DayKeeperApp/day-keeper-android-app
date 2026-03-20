package com.jsamuelsen11.daykeeper.feature.calendar.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperFloatingActionButton
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.calendar.component.CalendarMonthGrid
import com.jsamuelsen11.daykeeper.feature.calendar.component.DayEventsSheet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.koin.compose.viewmodel.koinViewModel

// region Constants

private val FilterRowHorizontalPadding = 12.dp
private val FilterRowVerticalPadding = 4.dp
private val FilterChipSpacing = 8.dp
private val ContentSpacing = 8.dp
private const val LABEL_MONTH = "Month"
private const val LABEL_WEEK = "Week"
private const val LABEL_DAY = "Day"
private const val LABEL_MANAGE_CALENDARS = "Manage Calendars"
private const val LABEL_MORE_OPTIONS = "More options"
private const val LABEL_GO_TO_TODAY = "Go to today"
private const val LABEL_PREV_MONTH = "Previous month"
private const val LABEL_NEXT_MONTH = "Next month"
private const val LABEL_CREATE_EVENT = "Create event"
private const val MONTH_DELTA_BACK = -1
private const val MONTH_DELTA_FORWARD = 1
private val MONTH_YEAR_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

// endregion

/**
 * Root composable for the calendar home screen.
 *
 * Observes [CalendarViewModel.uiState] and renders month/week/day views with a calendar filter row,
 * month navigation, a FAB for event creation, and an overflow menu to manage calendars.
 *
 * @param onEventClick Called with the event ID when the user taps an event.
 * @param onCreateEvent Called when the user taps the FAB with no specific date selected.
 * @param onCreateEventOnDate Called with epoch-millis when the user creates an event on a date.
 * @param onManageCalendars Called when the user selects "Manage Calendars" from the overflow menu.
 * @param modifier Optional [Modifier] applied to the root [Scaffold].
 * @param viewModel The [CalendarViewModel] provided by Koin; override in tests.
 */
@Composable
fun CalendarScreen(
  onEventClick: (String) -> Unit,
  onCreateEvent: () -> Unit,
  onCreateEventOnDate: (Long) -> Unit,
  onManageCalendars: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CalendarViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var menuExpanded by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier,
    topBar = {
      CalendarTopBar(
        uiState = uiState,
        menuExpanded = menuExpanded,
        onToggleMenu = { menuExpanded = !menuExpanded },
        onDismissMenu = { menuExpanded = false },
        onPrevMonth = { viewModel.navigateMonth(MONTH_DELTA_BACK) },
        onNextMonth = { viewModel.navigateMonth(MONTH_DELTA_FORWARD) },
        onGoToToday = viewModel::goToToday,
        onManageCalendars = {
          menuExpanded = false
          onManageCalendars()
        },
      )
    },
    floatingActionButton = {
      DayKeeperFloatingActionButton(
        onClick = {
          val state = uiState
          if (state is CalendarUiState.Success) {
            val millis =
              state.selectedDate
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            onCreateEventOnDate(millis)
          } else {
            onCreateEvent()
          }
        },
        icon = DayKeeperIcons.Add,
        contentDescription = LABEL_CREATE_EVENT,
      )
    },
  ) { innerPadding ->
    CalendarScreenContent(
      uiState = uiState,
      onViewModeChange = viewModel::setViewMode,
      onCalendarToggle = viewModel::toggleCalendarVisibility,
      onDateClick = viewModel::selectDate,
      onEventClick = onEventClick,
      modifier = Modifier.padding(innerPadding),
    )
  }
}

@Composable
private fun CalendarTopBar(
  uiState: CalendarUiState,
  menuExpanded: Boolean,
  onToggleMenu: () -> Unit,
  onDismissMenu: () -> Unit,
  onPrevMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onGoToToday: () -> Unit,
  onManageCalendars: () -> Unit,
) {
  val title =
    when (val state = uiState) {
      is CalendarUiState.Success -> state.currentMonth.format(MONTH_YEAR_FORMATTER)
      else -> ""
    }

  DayKeeperTopAppBar(
    title = title,
    actions = {
      IconButton(onClick = onPrevMonth) {
        Icon(imageVector = DayKeeperIcons.ChevronLeft, contentDescription = LABEL_PREV_MONTH)
      }
      IconButton(onClick = onNextMonth) {
        Icon(imageVector = DayKeeperIcons.ChevronRight, contentDescription = LABEL_NEXT_MONTH)
      }
      IconButton(onClick = onGoToToday) {
        Icon(imageVector = DayKeeperIcons.Today, contentDescription = LABEL_GO_TO_TODAY)
      }
      IconButton(onClick = onToggleMenu) {
        Icon(imageVector = DayKeeperIcons.MoreVert, contentDescription = LABEL_MORE_OPTIONS)
      }
      DropdownMenu(expanded = menuExpanded, onDismissRequest = onDismissMenu) {
        DropdownMenuItem(
          text = { Text(LABEL_MANAGE_CALENDARS) },
          onClick = onManageCalendars,
          leadingIcon = { Icon(imageVector = DayKeeperIcons.Calendar, contentDescription = null) },
        )
      }
    },
  )
}

@Composable
private fun CalendarScreenContent(
  uiState: CalendarUiState,
  onViewModeChange: (CalendarViewMode) -> Unit,
  onCalendarToggle: (String) -> Unit,
  onDateClick: (LocalDate) -> Unit,
  onEventClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (val state = uiState) {
    is CalendarUiState.Loading -> LoadingIndicator(modifier = modifier.fillMaxSize())
    is CalendarUiState.Error ->
      EmptyStateView(
        icon = DayKeeperIcons.Calendar,
        title = "Could not load calendar",
        body = state.message,
        modifier = modifier,
      )
    is CalendarUiState.Success ->
      CalendarSuccessContent(
        state = state,
        onViewModeChange = onViewModeChange,
        onCalendarToggle = onCalendarToggle,
        onDateClick = onDateClick,
        onEventClick = onEventClick,
        modifier = modifier,
      )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarSuccessContent(
  state: CalendarUiState.Success,
  onViewModeChange: (CalendarViewMode) -> Unit,
  onCalendarToggle: (String) -> Unit,
  onDateClick: (LocalDate) -> Unit,
  onEventClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(ContentSpacing),
  ) {
    ViewModeSegmentedRow(selected = state.viewMode, onSelect = onViewModeChange)

    CalendarFilterRow(calendars = state.calendars, onToggle = onCalendarToggle)

    when (state.viewMode) {
      CalendarViewMode.MONTH ->
        MonthViewContent(state = state, onDateClick = onDateClick, onEventClick = onEventClick)
      CalendarViewMode.WEEK ->
        WeekViewContent(
          selectedDate = state.selectedDate,
          events = state.selectedDateEvents,
          onEventClick = onEventClick,
          onDateClick = onDateClick,
        )
      CalendarViewMode.DAY ->
        DayViewContent(
          date = state.selectedDate,
          events = state.selectedDateEvents,
          onEventClick = onEventClick,
        )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewModeSegmentedRow(
  selected: CalendarViewMode,
  onSelect: (CalendarViewMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  val modes = CalendarViewMode.entries
  SingleChoiceSegmentedButtonRow(
    modifier = modifier.fillMaxWidth().padding(horizontal = FilterRowHorizontalPadding)
  ) {
    modes.forEachIndexed { index, mode ->
      SegmentedButton(
        selected = selected == mode,
        onClick = { onSelect(mode) },
        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
        label = {
          Text(
            text =
              when (mode) {
                CalendarViewMode.MONTH -> LABEL_MONTH
                CalendarViewMode.WEEK -> LABEL_WEEK
                CalendarViewMode.DAY -> LABEL_DAY
              }
          )
        },
      )
    }
  }
}

@Composable
private fun CalendarFilterRow(
  calendars: List<CalendarToggle>,
  onToggle: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyRow(
    modifier = modifier.fillMaxWidth(),
    contentPadding =
      PaddingValues(horizontal = FilterRowHorizontalPadding, vertical = FilterRowVerticalPadding),
    horizontalArrangement = Arrangement.spacedBy(FilterChipSpacing),
  ) {
    items(items = calendars, key = { it.calendar.calendarId }) { item ->
      FilterChip(
        selected = item.isVisible,
        onClick = { onToggle(item.calendar.calendarId) },
        label = { Text(item.calendar.name) },
        leadingIcon =
          if (item.isVisible) {
            { Icon(imageVector = DayKeeperIcons.Check, contentDescription = null) }
          } else {
            null
          },
      )
    }
  }
}

@Composable
private fun MonthViewContent(
  state: CalendarUiState.Success,
  onDateClick: (LocalDate) -> Unit,
  onEventClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize()) {
    CalendarMonthGrid(
      monthData = state.monthGrid,
      selectedDate = state.selectedDate,
      onDateClick = { date -> onDateClick(date) },
      modifier = Modifier.fillMaxWidth(),
    )
    Box(modifier = Modifier.weight(1f)) {
      DayEventsSheet(
        date = state.selectedDate,
        events = state.selectedDateEvents,
        onEventClick = onEventClick,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}
