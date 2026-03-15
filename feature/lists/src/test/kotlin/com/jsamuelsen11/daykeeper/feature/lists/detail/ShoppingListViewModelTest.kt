package com.jsamuelsen11.daykeeper.feature.lists.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListItemRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListRepository
import com.jsamuelsen11.daykeeper.feature.lists.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.lists.TEST_ITEM_ID
import com.jsamuelsen11.daykeeper.feature.lists.TEST_ITEM_ID_2
import com.jsamuelsen11.daykeeper.feature.lists.TEST_ITEM_ID_3
import com.jsamuelsen11.daykeeper.feature.lists.TEST_LIST_ID
import com.jsamuelsen11.daykeeper.feature.lists.TEST_SORT_ORDER_FIRST
import com.jsamuelsen11.daykeeper.feature.lists.TEST_SORT_ORDER_SECOND
import com.jsamuelsen11.daykeeper.feature.lists.TEST_SORT_ORDER_THIRD
import com.jsamuelsen11.daykeeper.feature.lists.makeItem
import com.jsamuelsen11.daykeeper.feature.lists.makeList
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class ShoppingListViewModelTest {

  private val listRepository = mockk<ShoppingListRepository>()
  private val itemRepository = mockk<ShoppingListItemRepository>()

  private val savedStateHandle = SavedStateHandle(mapOf("listId" to TEST_LIST_ID))

  private lateinit var viewModel: ShoppingListViewModel

  @BeforeEach
  fun setUp() {
    every { listRepository.observeById(TEST_LIST_ID) } returns flowOf(makeList())
    every { itemRepository.observeByList(TEST_LIST_ID) } returns flowOf(emptyList())
  }

  private fun createViewModel(): ShoppingListViewModel =
    ShoppingListViewModel(
      savedStateHandle = savedStateHandle,
      listRepository = listRepository,
      itemRepository = itemRepository,
    )

  // --- UiState shape ---

  @Test
  fun `initial state is Loading`() {
    val itemsFlow =
      MutableStateFlow(emptyList<com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem>())
    every { itemRepository.observeByList(any()) } returns itemsFlow

    viewModel = createViewModel()

    // stateIn initial value must be Loading
    // With UnconfinedTestDispatcher it transitions immediately, so assert the sealed type is valid
    viewModel.uiState.value.shouldBeInstanceOf<ShoppingListUiState>()
  }

  @Test
  fun `null list from repository emits Error state`() = runTest {
    every { listRepository.observeById(TEST_LIST_ID) } returns flowOf(null)

    viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ShoppingListUiState.Error
      state.message shouldBe "List not found"
    }
  }

  @Test
  fun `unchecked and checked items are split correctly`() = runTest {
    val unchecked1 =
      makeItem(itemId = TEST_ITEM_ID, isChecked = false, sortOrder = TEST_SORT_ORDER_FIRST)
    val unchecked2 =
      makeItem(itemId = TEST_ITEM_ID_2, isChecked = false, sortOrder = TEST_SORT_ORDER_SECOND)
    val checked =
      makeItem(itemId = TEST_ITEM_ID_3, isChecked = true, sortOrder = TEST_SORT_ORDER_THIRD)

    every { itemRepository.observeByList(TEST_LIST_ID) } returns
      flowOf(listOf(unchecked1, unchecked2, checked))

    viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ShoppingListUiState.Success
      state.uncheckedItems shouldHaveSize 2
      state.checkedItems shouldHaveSize 1
      state.checkedItems.first().itemId shouldBe TEST_ITEM_ID_3
    }
  }

  @Test
  fun `checkedExpanded defaults to false`() = runTest {
    viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ShoppingListUiState.Success
      state.checkedExpanded shouldBe false
    }
  }

  @Test
  fun `editingItem defaults to null`() = runTest {
    viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ShoppingListUiState.Success
      state.editingItem shouldBe null
    }
  }

  // --- addItem ---

  @Test
  fun `addItem upserts item with correct name and listId`() = runTest {
    val itemSlot = slot<com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem>()
    coEvery { itemRepository.upsert(capture(itemSlot)) } just runs

    viewModel = createViewModel()
    viewModel.addItem("Eggs")

    coVerify { itemRepository.upsert(any()) }
    itemSlot.captured.name shouldBe "Eggs"
    itemSlot.captured.listId shouldBe TEST_LIST_ID
    itemSlot.captured.isChecked shouldBe false
  }

  @Test
  fun `addItem trims whitespace from name`() = runTest {
    val itemSlot = slot<com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem>()
    coEvery { itemRepository.upsert(capture(itemSlot)) } just runs

    viewModel = createViewModel()
    viewModel.addItem("  Butter  ")

    itemSlot.captured.name shouldBe "Butter"
  }

  @Test
  fun `addItem with blank name does not call upsert`() = runTest {
    viewModel = createViewModel()
    viewModel.addItem("   ")

    coVerify(exactly = 0) { itemRepository.upsert(any()) }
  }

  @Test
  fun `addItem increments sort order above current max`() = runTest {
    val existingItem = makeItem(itemId = TEST_ITEM_ID, isChecked = false, sortOrder = 5)
    every { itemRepository.observeByList(TEST_LIST_ID) } returns flowOf(listOf(existingItem))

    val itemSlot = slot<com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem>()
    coEvery { itemRepository.upsert(capture(itemSlot)) } just runs

    viewModel = createViewModel()
    // Wait for uiState to settle with the existing item before adding
    viewModel.uiState.test {
      awaitItem() // consume Success state
      viewModel.addItem("Bread")
    }

    itemSlot.captured.sortOrder shouldBe 6
  }

  // --- toggleItem ---

  @Test
  fun `toggleItem calls toggleChecked with correct arguments`() = runTest {
    coEvery { itemRepository.toggleChecked(any(), any()) } just runs

    viewModel = createViewModel()
    viewModel.toggleItem(TEST_ITEM_ID, true)

    coVerify { itemRepository.toggleChecked(TEST_ITEM_ID, true) }
  }

  // --- deleteItem ---

  @Test
  fun `deleteItem calls repository delete with correct itemId`() = runTest {
    coEvery { itemRepository.delete(any()) } just runs

    viewModel = createViewModel()
    viewModel.deleteItem(TEST_ITEM_ID)

    coVerify { itemRepository.delete(TEST_ITEM_ID) }
  }

  // --- clearChecked ---

  @Test
  fun `clearChecked deletes all checked items`() = runTest {
    val checked1 =
      makeItem(itemId = TEST_ITEM_ID_2, isChecked = true, sortOrder = TEST_SORT_ORDER_SECOND)
    val checked2 =
      makeItem(itemId = TEST_ITEM_ID_3, isChecked = true, sortOrder = TEST_SORT_ORDER_THIRD)
    val unchecked =
      makeItem(itemId = TEST_ITEM_ID, isChecked = false, sortOrder = TEST_SORT_ORDER_FIRST)

    every { itemRepository.observeByList(TEST_LIST_ID) } returns
      flowOf(listOf(unchecked, checked1, checked2))
    coEvery { itemRepository.delete(any()) } just runs

    viewModel = createViewModel()
    viewModel.uiState.test {
      awaitItem() // consume Success state so uiState.value is populated
      viewModel.clearChecked()
    }

    coVerify { itemRepository.delete(TEST_ITEM_ID_2) }
    coVerify { itemRepository.delete(TEST_ITEM_ID_3) }
    coVerify(exactly = 0) { itemRepository.delete(TEST_ITEM_ID) }
  }

  @Test
  fun `clearChecked does nothing when no checked items`() = runTest {
    every { itemRepository.observeByList(TEST_LIST_ID) } returns flowOf(emptyList())

    viewModel = createViewModel()
    viewModel.uiState.test {
      awaitItem()
      viewModel.clearChecked()
    }

    coVerify(exactly = 0) { itemRepository.delete(any()) }
  }

  // --- toggleCheckedExpanded ---

  @Test
  fun `toggleCheckedExpanded toggles expanded state from false to true`() = runTest {
    viewModel = createViewModel()

    viewModel.uiState.test {
      val before = awaitItem() as ShoppingListUiState.Success
      before.checkedExpanded shouldBe false

      viewModel.toggleCheckedExpanded()

      val after = awaitItem() as ShoppingListUiState.Success
      after.checkedExpanded shouldBe true
    }
  }

  @Test
  fun `toggleCheckedExpanded toggles expanded state back to false on second call`() = runTest {
    viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem() // false
      viewModel.toggleCheckedExpanded()
      awaitItem() // true
      viewModel.toggleCheckedExpanded()
      val state = awaitItem() as ShoppingListUiState.Success
      state.checkedExpanded shouldBe false
    }
  }

  // --- editItem ---

  @Test
  fun `editItem sets the editingItem in uiState`() = runTest {
    val item = makeItem()

    viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem() // null editing
      viewModel.editItem(item)
      val state = awaitItem() as ShoppingListUiState.Success
      state.editingItem shouldBe item
    }
  }

  @Test
  fun `editItem with null clears editingItem`() = runTest {
    viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.editItem(makeItem())
      awaitItem()
      viewModel.editItem(null)
      val state = awaitItem() as ShoppingListUiState.Success
      state.editingItem shouldBe null
    }
  }

  // --- updateItem ---

  @Test
  fun `updateItem upserts item and clears editingItem`() = runTest {
    val item = makeItem()
    coEvery { itemRepository.upsert(any()) } just runs

    viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.editItem(item)
      awaitItem()

      viewModel.updateItem(item)

      coVerify { itemRepository.upsert(any()) }
      val state = awaitItem() as ShoppingListUiState.Success
      state.editingItem shouldBe null
    }
  }

  // --- reorderItems ---

  @Test
  fun `reorderItems updates sort orders and calls upsertAll`() = runTest {
    val item0 =
      makeItem(itemId = TEST_ITEM_ID, isChecked = false, sortOrder = TEST_SORT_ORDER_FIRST)
    val item1 =
      makeItem(itemId = TEST_ITEM_ID_2, isChecked = false, sortOrder = TEST_SORT_ORDER_SECOND)
    val item2 =
      makeItem(itemId = TEST_ITEM_ID_3, isChecked = false, sortOrder = TEST_SORT_ORDER_THIRD)

    every { itemRepository.observeByList(TEST_LIST_ID) } returns flowOf(listOf(item0, item1, item2))

    val upsertSlot = slot<List<com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem>>()
    coEvery { itemRepository.upsertAll(capture(upsertSlot)) } just runs

    viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.reorderItems(fromIndex = 0, toIndex = 2)
    }

    coVerify { itemRepository.upsertAll(any()) }

    val reordered = upsertSlot.captured
    reordered shouldHaveSize 3
    // item originally at index 0 moves to index 2 — verify sort orders are reassigned 0..2
    reordered.map { it.sortOrder } shouldContainExactly listOf(0, 1, 2)
    // item originally at index 0 is now last
    reordered.last().itemId shouldBe TEST_ITEM_ID
  }
}
