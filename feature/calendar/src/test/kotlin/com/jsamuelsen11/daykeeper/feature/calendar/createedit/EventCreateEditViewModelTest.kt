package com.jsamuelsen11.daykeeper.feature.calendar.createedit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventReminderRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventTypeRepository
import com.jsamuelsen11.daykeeper.feature.calendar.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_CALENDAR_ID
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_EVENT_ID
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_REMINDER_ID
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_SPACE_ID
import com.jsamuelsen11.daykeeper.feature.calendar.makeCalendar
import com.jsamuelsen11.daykeeper.feature.calendar.makeEvent
import com.jsamuelsen11.daykeeper.feature.calendar.makeEventReminder
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
class EventCreateEditViewModelTest {

  private val eventRepository = mockk<EventRepository>()
  private val calendarRepository = mockk<CalendarRepository>()
  private val eventTypeRepository = mockk<EventTypeRepository>()
  private val eventReminderRepository = mockk<EventReminderRepository>()

  @BeforeEach
  fun setUp() {
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(emptyList())
    every { eventTypeRepository.observeAll() } returns flowOf(emptyList())
    every { eventReminderRepository.observeByEvent(any()) } returns flowOf(emptyList())
    // getById is called during init in edit mode to load reminders.
    coEvery { eventReminderRepository.getById(any()) } returns null
  }

  private fun createModeViewModel(): EventCreateEditViewModel =
    EventCreateEditViewModel(
      savedStateHandle = SavedStateHandle(),
      eventRepository = eventRepository,
      calendarRepository = calendarRepository,
      eventTypeRepository = eventTypeRepository,
      eventReminderRepository = eventReminderRepository,
    )

  private fun editModeViewModel(eventId: String = TEST_EVENT_ID): EventCreateEditViewModel =
    EventCreateEditViewModel(
      savedStateHandle = SavedStateHandle(mapOf(EventCreateEditViewModel.KEY_EVENT_ID to eventId)),
      eventRepository = eventRepository,
      calendarRepository = calendarRepository,
      eventTypeRepository = eventTypeRepository,
      eventReminderRepository = eventReminderRepository,
    )

  // --- Create mode ---

  @Test
  fun `new event shows Ready with default form state`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventCreateEditUiState.Ready
      state.formState.title shouldBe ""
      state.isEditing shouldBe false
      state.formState.titleError shouldBe null
    }
  }

  @Test
  fun `create mode populates available calendars`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID, name = "Work")
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventCreateEditUiState.Ready
      state.calendars.size shouldBe 1
      state.calendars.first().calendarId shouldBe TEST_CALENDAR_ID
    }
  }

  @Test
  fun `create mode default calendar is pre-selected`() = runTest {
    val defaultCalendar = makeCalendar(calendarId = TEST_CALENDAR_ID, isDefault = true)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns
      flowOf(listOf(defaultCalendar))

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as EventCreateEditUiState.Ready
      state.formState.calendarId shouldBe TEST_CALENDAR_ID
    }
  }

  // --- Edit mode ---

  @Test
  fun `editing event loads existing data`() = runTest {
    val event =
      makeEvent(eventId = TEST_EVENT_ID, title = "Existing Event", calendarId = TEST_CALENDAR_ID)
    coEvery { eventRepository.getById(TEST_EVENT_ID) } returns event

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      // The init coroutine suspends on getById before the combine emits, so the
      // first item may be Loading. Skip it if necessary.
      val first = awaitItem()
      val state =
        if (first is EventCreateEditUiState.Ready) first
        else awaitItem() as EventCreateEditUiState.Ready
      state.formState.title shouldBe "Existing Event"
      state.formState.calendarId shouldBe TEST_CALENDAR_ID
      state.isEditing shouldBe true
    }
  }

  @Test
  fun `edit mode with missing event starts with empty form`() = runTest {
    coEvery { eventRepository.getById(TEST_EVENT_ID) } returns null

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val first = awaitItem()
      val state =
        if (first is EventCreateEditUiState.Ready) first
        else awaitItem() as EventCreateEditUiState.Ready
      state.formState.title shouldBe ""
      state.isEditing shouldBe true
    }
  }

  // --- onTitleChanged ---

  @Test
  fun `onTitleChanged updates title and clears error`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem() // initial Ready

      viewModel.onTitleChanged("New Event")

      val state = awaitItem() as EventCreateEditUiState.Ready
      state.formState.title shouldBe "New Event"
      state.formState.titleError shouldBe null
    }
  }

  @Test
  fun `onTitleChanged clears existing titleError`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave() // triggers title-empty error
      val errorState = awaitItem() as EventCreateEditUiState.Ready
      errorState.formState.titleError shouldNotBe null

      viewModel.onTitleChanged("Fixed")
      val clearedState = awaitItem() as EventCreateEditUiState.Ready
      clearedState.formState.titleError shouldBe null
    }
  }

  // --- onSave validation ---

  @Test
  fun `onSave with empty title sets error`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave()
      val state = awaitItem() as EventCreateEditUiState.Ready
      state.formState.titleError shouldBe EventCreateEditViewModel.TITLE_EMPTY_ERROR
    }
  }

  @Test
  fun `onSave with empty title does not call repository upsert`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave()
      awaitItem()
    }

    coVerify(exactly = 0) { eventRepository.upsert(any()) }
  }

  // --- onSave success (create) ---

  @Test
  fun `onSave with valid data calls upsert and emits Saved`() = runTest {
    val calendar = makeCalendar(calendarId = TEST_CALENDAR_ID)
    every { calendarRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(calendar))
    coEvery { eventRepository.upsert(any()) } just runs

    val viewModel = createModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem()
        viewModel.onTitleChanged("Team Meeting")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe EventCreateEditEvent.Saved
    }

    coVerify { eventRepository.upsert(any()) }
  }

  @Test
  fun `onSave in edit mode calls upsert and emits Saved`() = runTest {
    val event = makeEvent(eventId = TEST_EVENT_ID, calendarId = TEST_CALENDAR_ID)
    // getById is called twice: once in init to pre-populate the form, and once
    // inside buildEvent during onSave to fetch the latest version.
    coEvery { eventRepository.getById(TEST_EVENT_ID) } returns event
    coEvery { eventRepository.upsert(any()) } just runs

    val viewModel = editModeViewModel()

    // Wait for the ViewModel to reach Ready state before calling onSave.
    viewModel.uiState.test {
      val first = awaitItem()
      if (first !is EventCreateEditUiState.Ready) awaitItem()
      viewModel.onTitleChanged("Updated Title")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    viewModel.events.test { awaitItem() shouldBe EventCreateEditEvent.Saved }

    coVerify { eventRepository.upsert(any()) }
  }

  // --- onAllDayToggled ---

  @Test
  fun `onAllDayToggled to true clears time fields`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onAllDayToggled(true)
      val state = awaitItem() as EventCreateEditUiState.Ready
      state.formState.isAllDay shouldBe true
      state.formState.startAt shouldBe null
      state.formState.endAt shouldBe null
    }
  }

  @Test
  fun `onAllDayToggled to false clears date fields and sets time fields`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onAllDayToggled(true)
      awaitItem()
      viewModel.onAllDayToggled(false)
      val state = awaitItem() as EventCreateEditUiState.Ready
      state.formState.isAllDay shouldBe false
      state.formState.startDate shouldBe null
      state.formState.endDate shouldBe null
      state.formState.startAt shouldNotBe null
      state.formState.endAt shouldNotBe null
    }
  }

  // --- Reminders ---

  @Test
  fun `onAddReminder adds reminder`() = runTest {
    val minutesBefore = 15
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onAddReminder(minutesBefore)
      val state = awaitItem() as EventCreateEditUiState.Ready
      state.formState.reminders.size shouldBe 1
      state.formState.reminders.first().minutesBefore shouldBe minutesBefore
    }
  }

  @Test
  fun `onAddReminder does not add duplicate reminder`() = runTest {
    val minutesBefore = 15
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onAddReminder(minutesBefore)
      awaitItem()
      viewModel.onAddReminder(minutesBefore)
      // No second emission expected because no state change occurs.
      cancelAndIgnoreRemainingEvents()

      // verify still only 1
      val current = viewModel.uiState.value as EventCreateEditUiState.Ready
      current.formState.reminders.size shouldBe 1
    }
  }

  @Test
  fun `onRemoveReminder removes reminder`() = runTest {
    val minutesBefore = 15
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onAddReminder(minutesBefore)
      val withReminder = awaitItem() as EventCreateEditUiState.Ready
      val reminderId = withReminder.formState.reminders.first().id

      viewModel.onRemoveReminder(reminderId)
      val state = awaitItem() as EventCreateEditUiState.Ready
      state.formState.reminders shouldBe emptyList()
    }
  }

  @Test
  fun `existing reminders are loaded in edit mode`() = runTest {
    val event = makeEvent(eventId = TEST_EVENT_ID, calendarId = TEST_CALENDAR_ID)
    val reminder =
      makeEventReminder(reminderId = TEST_REMINDER_ID, eventId = TEST_EVENT_ID, minutesBefore = 30)
    coEvery { eventRepository.getById(TEST_EVENT_ID) } returns event
    // The ViewModel calls eventReminderRepository.getById(existingEvent.eventId).
    coEvery { eventReminderRepository.getById(TEST_EVENT_ID) } returns reminder

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      // Skip Loading if present, then land on Ready.
      val first = awaitItem()
      val state =
        if (first is EventCreateEditUiState.Ready) first
        else awaitItem() as EventCreateEditUiState.Ready
      state.formState.reminders.size shouldBe 1
      state.formState.reminders.first().minutesBefore shouldBe 30
    }
  }

  @Test
  fun `uiState type is EventCreateEditUiState`() = runTest {
    val viewModel = createModeViewModel()
    viewModel.uiState.value.shouldBeInstanceOf<EventCreateEditUiState>()
  }
}
