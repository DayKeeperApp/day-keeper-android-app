package com.jsamuelsen11.daykeeper.feature.calendar.home

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventTypeRepository
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatus
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatusProvider
import com.jsamuelsen11.daykeeper.feature.calendar.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_CALENDAR_ID
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_CALENDAR_ID_2
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_SPACE_ID
import com.jsamuelsen11.daykeeper.feature.calendar.makeCalendar
import com.jsamuelsen11.daykeeper.feature.calendar.makeEvent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class CalendarViewModelTest {

  private val eventRepository = mockk<EventRepository>()
  private val calendarRepository = mockk<CalendarRepository>()
  private val eventTypeRepository = mockk<EventTypeRepository>()
  private val syncStatusProvider =
    mockk<SyncStatusProvider> {
      every { syncStatus } returns kotlinx.coroutines.flow.MutableStateFlow(SyncStatus.Idle)
    }

  @BeforeEach
  fun setUp() {
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(emptyList())
    every { eventTypeRepository.observeAll() } returns flowOf(emptyList())
  }

  private fun createViewModel(): CalendarViewModel =
    CalendarViewModel(
      eventRepository = eventRepository,
      calendarRepository = calendarRepository,
      eventTypeRepository = eventTypeRepository,
      syncStatusProvider = syncStatusProvider,
    )

  @Test
  fun `empty calendars emits Success with empty grid`() = runTest {
    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarUiState.Success
      state.calendars shouldBe emptyList()
      state.selectedDateEvents shouldBe emptyList()
    }
  }

  @Test
  fun `single calendar with no events emits Success with empty selectedDateEvents`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))
    every { eventRepository.observeByCalendarAndRange(any(), any(), any()) } returns
      flowOf(emptyList())
    every { eventRepository.observeAllDayByCalendarAndRange(any(), any(), any()) } returns
      flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarUiState.Success
      state.selectedDateEvents shouldBe emptyList()
      state.calendars.size shouldBe 1
      state.calendars.first().calendar.calendarId shouldBe TEST_CALENDAR_ID
    }
  }

  @Test
  fun `events appear as dots on correct dates`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID, color = "#4285F4")
    // Build a timed event that starts on the first day of the current month so it
    // falls inside the currently-displayed month grid regardless of when the test
    // is executed.
    val firstOfMonth = java.time.LocalDate.now().withDayOfMonth(1)
    val startMillis =
      firstOfMonth.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endMillis = startMillis + 3_600_000L
    val event =
      makeEvent(
        calendarId = TEST_CALENDAR_ID,
        isAllDay = false,
        startAt = startMillis,
        endAt = endMillis,
      )

    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))
    every { eventRepository.observeByCalendarAndRange(TEST_CALENDAR_ID, any(), any()) } returns
      flowOf(listOf(event))
    every {
      eventRepository.observeAllDayByCalendarAndRange(TEST_CALENDAR_ID, any(), any())
    } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarUiState.Success
      // The month grid must contain at least one day cell with a non-empty dot list.
      val cellsWithDots = state.monthGrid.weeks.flatten().filter { it.eventDots.isNotEmpty() }
      cellsWithDots.size shouldBe 1
    }
  }

  @Test
  fun `navigateMonth changes currentMonth`() = runTest {
    val viewModel = createViewModel()
    val expected = YearMonth.now().plusMonths(1)

    viewModel.uiState.test {
      awaitItem() // initial
      viewModel.navigateMonth(1)
      val state = awaitItem() as CalendarUiState.Success
      state.currentMonth shouldBe expected
    }
  }

  @Test
  fun `navigateMonth backwards changes currentMonth`() = runTest {
    val viewModel = createViewModel()
    val expected = YearMonth.now().minusMonths(1)

    viewModel.uiState.test {
      awaitItem()
      viewModel.navigateMonth(-1)
      val state = awaitItem() as CalendarUiState.Success
      state.currentMonth shouldBe expected
    }
  }

  @Test
  fun `selectDate updates selected date and moves month if different`() = runTest {
    val viewModel = createViewModel()
    val futureDate = LocalDate.now().plusMonths(2).withDayOfMonth(1)

    viewModel.uiState.test {
      awaitItem() // initial state
      viewModel.selectDate(futureDate)
      // selectDate mutates selectedDate then currentMonth, producing two combine
      // emissions. Consume both and assert the final stable state.
      awaitItem() // intermediate: selectedDate updated, currentMonth still old
      val state = awaitItem() as CalendarUiState.Success
      state.selectedDate shouldBe futureDate
      state.currentMonth shouldBe YearMonth.from(futureDate)
    }
  }

  @Test
  fun `selectDate in same month only updates selectedDate`() = runTest {
    val viewModel = createViewModel()
    val today = LocalDate.now()
    val sameMonthDate = today.withDayOfMonth(1)

    viewModel.uiState.test {
      val initial = awaitItem() as CalendarUiState.Success
      val initialMonth = initial.currentMonth

      viewModel.selectDate(sameMonthDate)
      val state = awaitItem() as CalendarUiState.Success
      state.selectedDate shouldBe sameMonthDate
      state.currentMonth shouldBe initialMonth
    }
  }

  @Test
  fun `toggleCalendarVisibility hides a visible calendar`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))
    every { eventRepository.observeByCalendarAndRange(any(), any(), any()) } returns
      flowOf(emptyList())
    every { eventRepository.observeAllDayByCalendarAndRange(any(), any(), any()) } returns
      flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val initial = awaitItem() as CalendarUiState.Success
      initial.calendars.first().isVisible shouldBe true

      viewModel.toggleCalendarVisibility(TEST_CALENDAR_ID)
      val state = awaitItem() as CalendarUiState.Success
      state.calendars.first().isVisible shouldBe false
    }
  }

  @Test
  fun `toggleCalendarVisibility shows a hidden calendar`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))
    every { eventRepository.observeByCalendarAndRange(any(), any(), any()) } returns
      flowOf(emptyList())
    every { eventRepository.observeAllDayByCalendarAndRange(any(), any(), any()) } returns
      flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.toggleCalendarVisibility(TEST_CALENDAR_ID)
      awaitItem() // hidden
      viewModel.toggleCalendarVisibility(TEST_CALENDAR_ID)
      val state = awaitItem() as CalendarUiState.Success
      state.calendars.first().isVisible shouldBe true
    }
  }

  @Test
  fun `goToToday resets to current date`() = runTest {
    val viewModel = createViewModel()
    val futureDate = LocalDate.now().plusMonths(3)

    viewModel.uiState.test {
      awaitItem() // initial
      viewModel.selectDate(futureDate)
      // selectDate produces two emissions (selectedDate change, then currentMonth change).
      awaitItem()
      awaitItem()

      viewModel.goToToday()
      // goToToday produces two emissions (selectedDate reset, then currentMonth reset).
      awaitItem()
      val state = awaitItem() as CalendarUiState.Success
      state.selectedDate shouldBe LocalDate.now()
      state.currentMonth shouldBe YearMonth.now()
    }
  }

  @Test
  fun `setViewMode switches mode`() = runTest {
    val viewModel = createViewModel()

    viewModel.uiState.test {
      val initial = awaitItem() as CalendarUiState.Success
      initial.viewMode shouldBe CalendarViewMode.MONTH

      viewModel.setViewMode(CalendarViewMode.WEEK)
      val week = awaitItem() as CalendarUiState.Success
      week.viewMode shouldBe CalendarViewMode.WEEK

      viewModel.setViewMode(CalendarViewMode.DAY)
      val day = awaitItem() as CalendarUiState.Success
      day.viewMode shouldBe CalendarViewMode.DAY
    }
  }

  @Test
  fun `deleted events are filtered out`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID)
    val deletedEvent = makeEvent(calendarId = TEST_CALENDAR_ID, deletedAt = TEST_UPDATED_AT)

    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))
    every { eventRepository.observeByCalendarAndRange(TEST_CALENDAR_ID, any(), any()) } returns
      flowOf(listOf(deletedEvent))
    every {
      eventRepository.observeAllDayByCalendarAndRange(TEST_CALENDAR_ID, any(), any())
    } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarUiState.Success
      val cellsWithDots = state.monthGrid.weeks.flatten().filter { it.eventDots.isNotEmpty() }
      cellsWithDots shouldBe emptyList()
    }
  }

  @Test
  fun `deleted calendars are excluded from the Success state`() = runTest {
    val deleted = makeCalendar(calendarId = TEST_CALENDAR_ID, deletedAt = TEST_UPDATED_AT)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(deleted))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarUiState.Success
      state.calendars shouldBe emptyList()
    }
  }

  @Test
  fun `multiple calendars are listed sorted by name`() = runTest {
    val calA = makeCalendar(calendarId = TEST_CALENDAR_ID, name = "Zebra")
    val calB = makeCalendar(calendarId = TEST_CALENDAR_ID_2, name = "Alpha", isDefault = false)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calA, calB))
    every { eventRepository.observeByCalendarAndRange(any(), any(), any()) } returns
      flowOf(emptyList())
    every { eventRepository.observeAllDayByCalendarAndRange(any(), any(), any()) } returns
      flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarUiState.Success
      state.calendars.first().calendar.name shouldBe "Alpha"
      state.calendars.last().calendar.name shouldBe "Zebra"
    }
  }

  @Test
  fun `uiState type is CalendarUiState`() = runTest {
    val viewModel = createViewModel()
    viewModel.uiState.value.shouldBeInstanceOf<CalendarUiState>()
  }
}

private const val TEST_UPDATED_AT = 2_000L
