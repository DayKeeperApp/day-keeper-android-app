package com.jsamuelsen11.daykeeper.feature.calendar.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.attachment.AttachmentManager
import com.jsamuelsen11.daykeeper.core.data.repository.AttachmentRepository
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventReminderRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventTypeRepository
import com.jsamuelsen11.daykeeper.feature.calendar.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_CALENDAR_ID
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_EVENT_ID
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_EVENT_TYPE_ID
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_REMINDER_ID
import com.jsamuelsen11.daykeeper.feature.calendar.makeCalendar
import com.jsamuelsen11.daykeeper.feature.calendar.makeEvent
import com.jsamuelsen11.daykeeper.feature.calendar.makeEventReminder
import com.jsamuelsen11.daykeeper.feature.calendar.makeEventType
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
class EventDetailViewModelTest {

  private val eventRepository = mockk<EventRepository>()
  private val calendarRepository = mockk<CalendarRepository>()
  private val eventTypeRepository = mockk<EventTypeRepository>()
  private val eventReminderRepository = mockk<EventReminderRepository>()
  private val attachmentRepository: AttachmentRepository = mockk(relaxed = true)
  private val attachmentManager: AttachmentManager = mockk(relaxed = true)

  private val savedStateHandle = SavedStateHandle(mapOf("eventId" to TEST_EVENT_ID))

  @BeforeEach
  fun setUp() {
    every { eventRepository.observeById(TEST_EVENT_ID) } returns flowOf(makeEvent())
    every { calendarRepository.observeById(TEST_CALENDAR_ID) } returns flowOf(makeCalendar())
    every { eventTypeRepository.observeAll() } returns flowOf(emptyList())
    every { eventReminderRepository.observeByEvent(TEST_EVENT_ID) } returns flowOf(emptyList())
    every { attachmentRepository.observeByEntity(any(), any()) } returns flowOf(emptyList())
  }

  private fun createViewModel(): EventDetailViewModel =
    EventDetailViewModel(
      savedStateHandle = savedStateHandle,
      eventRepository = eventRepository,
      calendarRepository = calendarRepository,
      eventTypeRepository = eventTypeRepository,
      eventReminderRepository = eventReminderRepository,
      attachmentRepository = attachmentRepository,
      attachmentManager = attachmentManager,
    )

  @Test
  fun `loads event with related entities on init`() = runTest {
    val event = makeEvent(eventId = TEST_EVENT_ID, title = "Stand-up")
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID, name = "Work")
    every { eventRepository.observeById(TEST_EVENT_ID) } returns flowOf(event)
    every { calendarRepository.observeById(TEST_CALENDAR_ID) } returns flowOf(calendar)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventDetailUiState.Success
      state.event.eventId shouldBe TEST_EVENT_ID
      state.event.title shouldBe "Stand-up"
      state.calendar shouldNotBe null
      state.calendar?.name shouldBe "Work"
      state.reminders shouldBe emptyList()
    }
  }

  @Test
  fun `emits Error when event not found`() = runTest {
    every { eventRepository.observeById(TEST_EVENT_ID) } returns flowOf(null)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventDetailUiState.Error
      state.message shouldBe "Event not found"
    }
  }

  @Test
  fun `emits Error when event is soft-deleted`() = runTest {
    val deletedEvent = makeEvent(deletedAt = 3_000L)
    every { eventRepository.observeById(TEST_EVENT_ID) } returns flowOf(deletedEvent)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventDetailUiState.Error
      state.message shouldBe "Event not found"
    }
  }

  @Test
  fun `resolves eventType when event has an eventTypeId`() = runTest {
    val event = makeEvent(eventTypeId = TEST_EVENT_TYPE_ID)
    val eventType = makeEventType(eventTypeId = TEST_EVENT_TYPE_ID, name = "Meeting")
    every { eventRepository.observeById(TEST_EVENT_ID) } returns flowOf(event)
    every { eventTypeRepository.observeAll() } returns flowOf(listOf(eventType))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventDetailUiState.Success
      state.eventType shouldNotBe null
      state.eventType?.eventTypeId shouldBe TEST_EVENT_TYPE_ID
    }
  }

  @Test
  fun `eventType is null when event has no eventTypeId`() = runTest {
    val event = makeEvent(eventTypeId = null)
    every { eventRepository.observeById(TEST_EVENT_ID) } returns flowOf(event)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventDetailUiState.Success
      state.eventType shouldBe null
    }
  }

  @Test
  fun `reminders are listed in Success state`() = runTest {
    val reminder = makeEventReminder(reminderId = TEST_REMINDER_ID, minutesBefore = 15)
    every { eventReminderRepository.observeByEvent(TEST_EVENT_ID) } returns flowOf(listOf(reminder))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventDetailUiState.Success
      state.reminders.size shouldBe 1
      state.reminders.first().reminderId shouldBe TEST_REMINDER_ID
    }
  }

  @Test
  fun `deleted reminders are excluded from Success state`() = runTest {
    val deletedReminder = makeEventReminder(deletedAt = 3_000L)
    every { eventReminderRepository.observeByEvent(TEST_EVENT_ID) } returns
      flowOf(listOf(deletedReminder))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventDetailUiState.Success
      state.reminders shouldBe emptyList()
    }
  }

  @Test
  fun `calendar is null when calendar is soft-deleted`() = runTest {
    val deletedCalendar = makeCalendar(deletedAt = 3_000L)
    every { calendarRepository.observeById(TEST_CALENDAR_ID) } returns flowOf(deletedCalendar)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventDetailUiState.Success
      state.calendar shouldBe null
    }
  }

  @Test
  fun `deleteEvent calls repository delete`() = runTest {
    coEvery { eventRepository.delete(any()) } just runs

    val viewModel = createViewModel()
    viewModel.deleteEvent()

    coVerify { eventRepository.delete(TEST_EVENT_ID) }
  }

  @Test
  fun `repository error emits Error state`() = runTest {
    every { eventRepository.observeById(TEST_EVENT_ID) } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException("Stream failed") }

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventDetailUiState.Error
      state.message shouldBe "Stream failed"
    }
  }

  @Test
  fun `uiState type is EventDetailUiState`() = runTest {
    val viewModel = createViewModel()
    viewModel.uiState.value.shouldBeInstanceOf<EventDetailUiState>()
  }
}
