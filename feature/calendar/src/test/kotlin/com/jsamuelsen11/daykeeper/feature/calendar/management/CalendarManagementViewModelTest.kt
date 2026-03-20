package com.jsamuelsen11.daykeeper.feature.calendar.management

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.feature.calendar.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_CALENDAR_ID
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_CALENDAR_ID_2
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_SPACE_ID
import com.jsamuelsen11.daykeeper.feature.calendar.makeCalendar
import com.jsamuelsen11.daykeeper.feature.calendar.makeEvent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class CalendarManagementViewModelTest {

  private val calendarRepository = mockk<CalendarRepository>()
  private val eventRepository = mockk<EventRepository>()

  @BeforeEach
  fun setUp() {
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(emptyList())
    every { eventRepository.observeByCalendar(TEST_SPACE_ID) } returns flowOf(emptyList())
  }

  private fun createViewModel(): CalendarManagementViewModel =
    CalendarManagementViewModel(
      calendarRepository = calendarRepository,
      eventRepository = eventRepository,
    )

  @Test
  fun `empty repository emits Success with empty items`() = runTest {
    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarManagementUiState.Success
      state.items shouldBe emptyList()
    }
  }

  @Test
  fun `loads calendars with event counts`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID, name = "Work")
    val event = makeEvent(calendarId = TEST_CALENDAR_ID)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))
    every { eventRepository.observeByCalendar(TEST_SPACE_ID) } returns flowOf(listOf(event))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarManagementUiState.Success
      state.items.size shouldBe 1
      state.items.first().calendar.calendarId shouldBe TEST_CALENDAR_ID
      state.items.first().eventCount shouldBe 1
    }
  }

  @Test
  fun `deleted calendars are excluded from items`() = runTest {
    val deleted = makeCalendar(calendarId = TEST_CALENDAR_ID, deletedAt = 3_000L)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(deleted))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarManagementUiState.Success
      state.items shouldBe emptyList()
    }
  }

  @Test
  fun `deleted events are not counted`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID)
    val deletedEvent = makeEvent(calendarId = TEST_CALENDAR_ID, deletedAt = 3_000L)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))
    every { eventRepository.observeByCalendar(TEST_SPACE_ID) } returns flowOf(listOf(deletedEvent))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarManagementUiState.Success
      state.items.first().eventCount shouldBe 0
    }
  }

  @Test
  fun `default calendar appears before non-default calendars`() = runTest {
    val defaultCal = makeCalendar(calendarId = TEST_CALENDAR_ID, name = "Zebra", isDefault = true)
    val regularCal =
      makeCalendar(calendarId = TEST_CALENDAR_ID_2, name = "Alpha", isDefault = false)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns
      flowOf(listOf(regularCal, defaultCal))
    every { eventRepository.observeByCalendar(TEST_SPACE_ID) } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarManagementUiState.Success
      state.items.first().calendar.isDefault shouldBe true
    }
  }

  @Test
  fun `non-default calendars are sorted by name`() = runTest {
    val calB = makeCalendar(calendarId = TEST_CALENDAR_ID, name = "Zeta", isDefault = false)
    val calA = makeCalendar(calendarId = TEST_CALENDAR_ID_2, name = "Alpha", isDefault = false)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calB, calA))
    every { eventRepository.observeByCalendar(TEST_SPACE_ID) } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarManagementUiState.Success
      state.items.first().calendar.name shouldBe "Alpha"
      state.items.last().calendar.name shouldBe "Zeta"
    }
  }

  @Test
  fun `deleteCalendar calls repository`() = runTest {
    coEvery { calendarRepository.delete(any()) } just runs

    val viewModel = createViewModel()
    viewModel.deleteCalendar(TEST_CALENDAR_ID)

    coVerify { calendarRepository.delete(TEST_CALENDAR_ID) }
  }

  @Test
  fun `repository error emits Error state`() = runTest {
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException("DB broken") }

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarManagementUiState.Error
      state.message shouldBe "DB broken"
    }
  }

  @Test
  fun `uiState type is CalendarManagementUiState`() = runTest {
    val viewModel = createViewModel()
    viewModel.uiState.value.shouldBeInstanceOf<CalendarManagementUiState>()
  }
}
