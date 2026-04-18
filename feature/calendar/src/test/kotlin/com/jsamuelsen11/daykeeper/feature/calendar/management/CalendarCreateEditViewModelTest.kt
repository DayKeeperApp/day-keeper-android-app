package com.jsamuelsen11.daykeeper.feature.calendar.management

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import com.jsamuelsen11.daykeeper.feature.calendar.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_CALENDAR_ID
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_SPACE_ID
import com.jsamuelsen11.daykeeper.feature.calendar.TEST_TENANT_ID
import com.jsamuelsen11.daykeeper.feature.calendar.makeCalendar
import com.jsamuelsen11.daykeeper.feature.calendar.navigation.CalendarCreateEditRoute
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class CalendarCreateEditViewModelTest {

  private val calendarRepository = mockk<CalendarRepository>()
  private val savedStateHandle = mockk<SavedStateHandle>()
  private val sessionProvider =
    mockk<CurrentSessionProvider> {
      every { spaceId } returns TEST_SPACE_ID
      every { tenantId } returns TEST_TENANT_ID
    }

  @BeforeEach
  fun setUp() {
    mockkStatic("androidx.navigation.SavedStateHandleKt")
  }

  @AfterEach
  fun tearDown() {
    unmockkStatic("androidx.navigation.SavedStateHandleKt")
  }

  private fun createModeViewModel(): CalendarCreateEditViewModel {
    every { savedStateHandle.toRoute<CalendarCreateEditRoute>() } returns
      CalendarCreateEditRoute(calendarId = null)
    return CalendarCreateEditViewModel(
      savedStateHandle = savedStateHandle,
      calendarRepository = calendarRepository,
      sessionProvider = sessionProvider,
    )
  }

  private fun editModeViewModel(
    calendarId: String = TEST_CALENDAR_ID
  ): CalendarCreateEditViewModel {
    every { savedStateHandle.toRoute<CalendarCreateEditRoute>() } returns
      CalendarCreateEditRoute(calendarId = calendarId)
    return CalendarCreateEditViewModel(
      savedStateHandle = savedStateHandle,
      calendarRepository = calendarRepository,
      sessionProvider = sessionProvider,
    )
  }

  // --- Create mode ---

  @Test
  fun `new calendar shows Ready with defaults`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarCreateEditUiState.Ready
      state.name shouldBe ""
      state.isEditing shouldBe false
      state.nameError shouldBe null
    }
  }

  @Test
  fun `create mode starts with no nameError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarCreateEditUiState.Ready
      state.nameError shouldBe null
    }
  }

  // --- Edit mode ---

  @Test
  fun `editing existing calendar loads its data`() = runTest {
    coEvery { calendarRepository.getById(TEST_CALENDAR_ID) } returns
      makeCalendar(calendarId = TEST_CALENDAR_ID, name = "My Calendar", color = "#FF0000")

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarCreateEditUiState.Ready
      state.name shouldBe "My Calendar"
      state.color shouldBe "#FF0000"
      state.isEditing shouldBe true
    }
  }

  @Test
  fun `edit mode with missing calendar starts with empty name`() = runTest {
    coEvery { calendarRepository.getById(TEST_CALENDAR_ID) } returns null

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CalendarCreateEditUiState.Ready
      state.name shouldBe ""
      state.isEditing shouldBe true
    }
  }

  // --- onNameChanged ---

  @Test
  fun `onNameChanged updates name`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onNameChanged("Personal")
      val state = awaitItem() as CalendarCreateEditUiState.Ready
      state.name shouldBe "Personal"
    }
  }

  @Test
  fun `onNameChanged clears existing nameError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave() // trigger name-required error
      val errorState = awaitItem() as CalendarCreateEditUiState.Ready
      errorState.nameError shouldBe CalendarCreateEditViewModel.NAME_REQUIRED_ERROR

      viewModel.onNameChanged("Fixed")
      val clearedState = awaitItem() as CalendarCreateEditUiState.Ready
      clearedState.nameError shouldBe null
    }
  }

  // --- onSave validation ---

  @Test
  fun `onSave with blank name sets error`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave()
      val state = awaitItem() as CalendarCreateEditUiState.Ready
      state.nameError shouldBe CalendarCreateEditViewModel.NAME_REQUIRED_ERROR
    }
  }

  @Test
  fun `onSave with blank name does not call repository upsert`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave()
      awaitItem()
    }

    coVerify(exactly = 0) { calendarRepository.upsert(any()) }
  }

  // --- onSave success (create) ---

  @Test
  fun `onSave with valid name calls upsert and emits Saved`() = runTest {
    coEvery { calendarRepository.upsert(any()) } just runs

    val viewModel = createModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem()
        viewModel.onNameChanged("Family")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe CalendarCreateEditEvent.Saved
    }

    coVerify { calendarRepository.upsert(any()) }
  }

  @Test
  fun `onSave in create mode upserts with trimmed name`() = runTest {
    val upsertSlot = io.mockk.slot<com.jsamuelsen11.daykeeper.core.model.calendar.Calendar>()
    coEvery { calendarRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onNameChanged("  Family  ")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    upsertSlot.captured.name shouldBe "Family"
    upsertSlot.captured.normalizedName shouldBe "family"
  }

  // --- onSave success (edit) ---

  @Test
  fun `onSave in edit mode calls upsert and emits Saved`() = runTest {
    coEvery { calendarRepository.getById(TEST_CALENDAR_ID) } returns
      makeCalendar(calendarId = TEST_CALENDAR_ID, name = "Old Name")
    coEvery { calendarRepository.upsert(any()) } just runs

    val viewModel = editModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem()
        viewModel.onNameChanged("New Name")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe CalendarCreateEditEvent.Saved
    }

    coVerify { calendarRepository.upsert(any()) }
  }

  @Test
  fun `onSave in edit mode preserves calendarId`() = runTest {
    coEvery { calendarRepository.getById(TEST_CALENDAR_ID) } returns
      makeCalendar(calendarId = TEST_CALENDAR_ID, name = "Old")
    val upsertSlot = io.mockk.slot<com.jsamuelsen11.daykeeper.core.model.calendar.Calendar>()
    coEvery { calendarRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onNameChanged("Updated")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    upsertSlot.captured.calendarId shouldBe TEST_CALENDAR_ID
    upsertSlot.captured.name shouldBe "Updated"
  }

  @Test
  fun `uiState type is CalendarCreateEditUiState`() = runTest {
    val viewModel = createModeViewModel()
    viewModel.uiState.value.shouldBeInstanceOf<CalendarCreateEditUiState>()
  }
}
