package com.jsamuelsen11.daykeeper.feature.lists.createedit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListRepository
import com.jsamuelsen11.daykeeper.feature.lists.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.lists.TEST_LIST_ID
import com.jsamuelsen11.daykeeper.feature.lists.makeList
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class ListCreateEditViewModelTest {

  private val listRepository = mockk<ShoppingListRepository>()

  private fun createModeViewModel(): ListCreateEditViewModel =
    ListCreateEditViewModel(savedStateHandle = SavedStateHandle(), listRepository = listRepository)

  private fun editModeViewModel(listId: String = TEST_LIST_ID): ListCreateEditViewModel =
    ListCreateEditViewModel(
      savedStateHandle = SavedStateHandle(mapOf("listId" to listId)),
      listRepository = listRepository,
    )

  // --- Create mode ---

  @Test
  fun `create mode transitions from Loading to Ready`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test { awaitItem().shouldBeInstanceOf<ListCreateEditUiState.Ready>() }
  }

  @Test
  fun `create mode starts with empty name`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ListCreateEditUiState.Ready
      state.name shouldBe ""
    }
  }

  @Test
  fun `create mode has isEditing false`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ListCreateEditUiState.Ready
      state.isEditing shouldBe false
    }
  }

  @Test
  fun `create mode starts with no name error`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ListCreateEditUiState.Ready
      state.nameError shouldBe null
    }
  }

  // --- Edit mode ---

  @Test
  fun `edit mode loads existing list name`() = runTest {
    coEvery { listRepository.getById(TEST_LIST_ID) } returns makeList(name = "Weekly Shop")

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ListCreateEditUiState.Ready
      state.name shouldBe "Weekly Shop"
    }
  }

  @Test
  fun `edit mode has isEditing true`() = runTest {
    coEvery { listRepository.getById(TEST_LIST_ID) } returns makeList()

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ListCreateEditUiState.Ready
      state.isEditing shouldBe true
    }
  }

  @Test
  fun `edit mode with missing list loads empty name`() = runTest {
    coEvery { listRepository.getById(TEST_LIST_ID) } returns null

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ListCreateEditUiState.Ready
      state.name shouldBe ""
    }
  }

  // --- onNameChanged ---

  @Test
  fun `onNameChanged updates name in Ready state`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem() // initial Ready

      viewModel.onNameChanged("Pharmacy")

      val state = awaitItem() as ListCreateEditUiState.Ready
      state.name shouldBe "Pharmacy"
    }
  }

  @Test
  fun `onNameChanged clears existing nameError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()

      // Trigger validation error first
      viewModel.onSave()
      val errorState = awaitItem() as ListCreateEditUiState.Ready
      errorState.nameError shouldBe "Name cannot be empty"

      // Typing should clear the error
      viewModel.onNameChanged("Bakery")
      val clearedState = awaitItem() as ListCreateEditUiState.Ready
      clearedState.nameError shouldBe null
    }
  }

  // --- onSave validation ---

  @Test
  fun `onSave with blank name sets nameError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()

      viewModel.onSave()

      val state = awaitItem() as ListCreateEditUiState.Ready
      state.nameError shouldBe "Name cannot be empty"
    }
  }

  @Test
  fun `onSave with whitespace-only name sets nameError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onNameChanged("   ")
      awaitItem()

      viewModel.onSave()

      val state = awaitItem() as ListCreateEditUiState.Ready
      state.nameError shouldBe "Name cannot be empty"
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

    coVerify(exactly = 0) { listRepository.upsert(any()) }
  }

  // --- onSave success (create) ---

  @Test
  fun `onSave calls upsert and emits Saved event in create mode`() = runTest {
    coEvery { listRepository.upsert(any()) } just runs

    val viewModel = createModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem()
        viewModel.onNameChanged("Fruit & Veg")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe ListCreateEditEvent.Saved
    }

    coVerify { listRepository.upsert(any()) }
  }

  @Test
  fun `onSave in create mode upserts list with trimmed name`() = runTest {
    val upsertSlot = slot<com.jsamuelsen11.daykeeper.core.model.list.ShoppingList>()
    coEvery { listRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onNameChanged("  Fish Market  ")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    upsertSlot.captured.name shouldBe "Fish Market"
    upsertSlot.captured.normalizedName shouldBe "fish market"
  }

  // --- onSave success (edit) ---

  @Test
  fun `onSave calls upsert and emits Saved event in edit mode`() = runTest {
    coEvery { listRepository.getById(TEST_LIST_ID) } returns makeList(name = "Old Name")
    coEvery { listRepository.upsert(any()) } just runs

    val viewModel = editModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem()
        viewModel.onNameChanged("New Name")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe ListCreateEditEvent.Saved
    }

    coVerify { listRepository.upsert(any()) }
  }

  @Test
  fun `onSave in edit mode upserts with updated name`() = runTest {
    coEvery { listRepository.getById(TEST_LIST_ID) } returns makeList(name = "Old Name")

    val upsertSlot = slot<com.jsamuelsen11.daykeeper.core.model.list.ShoppingList>()
    coEvery { listRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onNameChanged("Updated Name")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    upsertSlot.captured.name shouldBe "Updated Name"
    upsertSlot.captured.listId shouldBe TEST_LIST_ID
  }

  @Test
  fun `onSave in edit mode with missing list does not upsert`() = runTest {
    coEvery { listRepository.getById(TEST_LIST_ID) } returns null

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onNameChanged("Some Name")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    coVerify(exactly = 0) { listRepository.upsert(any()) }
  }

  @Test
  fun `onSave in edit mode with missing list resets isSaving`() = runTest {
    coEvery { listRepository.getById(TEST_LIST_ID) } returns makeList(name = "Exists")

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      awaitItem() // Ready with loaded name

      // Now make getById return null for the save attempt
      coEvery { listRepository.getById(TEST_LIST_ID) } returns null

      viewModel.onNameChanged("Updated")
      awaitItem()
      viewModel.onSave()

      // Should transition through isSaving=true then back to isSaving=false
      val savingState = awaitItem() as ListCreateEditUiState.Ready
      if (savingState.isSaving) {
        val resetState = awaitItem() as ListCreateEditUiState.Ready
        resetState.isSaving shouldBe false
      } else {
        savingState.isSaving shouldBe false
      }
    }
  }

  @Test
  fun `onSave resets isSaving and sets error on repository failure`() = runTest {
    coEvery { listRepository.upsert(any()) } throws RuntimeException("DB error")

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onNameChanged("Valid Name")
      awaitItem()
      viewModel.onSave()

      // Should transition through isSaving=true then back to isSaving=false with error
      val savingState = awaitItem() as ListCreateEditUiState.Ready
      if (savingState.isSaving) {
        val resetState = awaitItem() as ListCreateEditUiState.Ready
        resetState.isSaving shouldBe false
        resetState.nameError shouldBe "DB error"
      } else {
        savingState.isSaving shouldBe false
        savingState.nameError shouldBe "DB error"
      }
    }
  }
}
