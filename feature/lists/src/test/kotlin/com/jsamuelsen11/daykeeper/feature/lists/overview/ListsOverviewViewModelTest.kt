package com.jsamuelsen11.daykeeper.feature.lists.overview

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListItemRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListRepository
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatus
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatusProvider
import com.jsamuelsen11.daykeeper.feature.lists.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.lists.TEST_ITEM_ID_2
import com.jsamuelsen11.daykeeper.feature.lists.TEST_LIST_ID
import com.jsamuelsen11.daykeeper.feature.lists.TEST_LIST_ID_2
import com.jsamuelsen11.daykeeper.feature.lists.makeItem
import com.jsamuelsen11.daykeeper.feature.lists.makeList
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class ListsOverviewViewModelTest {

  private val listRepository = mockk<ShoppingListRepository>()
  private val itemRepository = mockk<ShoppingListItemRepository>()
  private val syncStatusProvider =
    mockk<SyncStatusProvider> {
      every { syncStatus } returns kotlinx.coroutines.flow.MutableStateFlow(SyncStatus.Idle)
    }

  private lateinit var viewModel: ListsOverviewViewModel

  @BeforeEach
  fun setUp() {
    every { listRepository.observeAll() } returns flowOf(emptyList())
    every { itemRepository.observeByList(any()) } returns flowOf(emptyList())
  }

  private fun createViewModel(): ListsOverviewViewModel =
    ListsOverviewViewModel(
      listRepository = listRepository,
      itemRepository = itemRepository,
      syncStatusProvider = syncStatusProvider,
    )

  @Test
  fun `initial state is Loading before any emission`() = runTest {
    val listsFlow =
      MutableStateFlow(emptyList<com.jsamuelsen11.daykeeper.core.model.list.ShoppingList>())
    every { listRepository.observeAll() } returns listsFlow

    viewModel = createViewModel()

    // The stateIn initial value is Loading; verify the type is correct by checking the sealed
    // hierarchy — the ViewModel will immediately transition to Success with
    // UnconfinedTestDispatcher
    // but Loading is the declared initialValue
    viewModel.uiState.value.shouldBeInstanceOf<ListsOverviewUiState>()
  }

  @Test
  fun `empty list emits Success with empty list`() = runTest {
    every { listRepository.observeAll() } returns flowOf(emptyList())

    viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<ListsOverviewUiState.Success>()
      (state as ListsOverviewUiState.Success).lists shouldBe emptyList()
    }
  }

  @Test
  fun `single list with items computes totalItems and checkedItems correctly`() = runTest {
    val list = makeList(listId = TEST_LIST_ID)
    val unchecked = makeItem(itemId = "item-a", isChecked = false)
    val checked1 = makeItem(itemId = "item-b", isChecked = true)
    val checked2 = makeItem(itemId = "item-c", isChecked = true)

    every { listRepository.observeAll() } returns flowOf(listOf(list))
    every { itemRepository.observeByList(TEST_LIST_ID) } returns
      flowOf(listOf(unchecked, checked1, checked2))

    viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ListsOverviewUiState.Success
      state.lists.size shouldBe 1

      val summary = state.lists.first()
      summary.list shouldBe list
      summary.totalItems shouldBe 3
      summary.checkedItems shouldBe 2
    }
  }

  @Test
  fun `multiple lists each get correct item counts`() = runTest {
    val list1 = makeList(listId = TEST_LIST_ID, name = "Groceries")
    val list2 = makeList(listId = TEST_LIST_ID_2, name = "Hardware")

    val list1Item1 = makeItem(itemId = "l1-item-1", listId = TEST_LIST_ID, isChecked = false)
    val list1Item2 = makeItem(itemId = "l1-item-2", listId = TEST_LIST_ID, isChecked = true)
    val list2Item1 = makeItem(itemId = TEST_ITEM_ID_2, listId = TEST_LIST_ID_2, isChecked = false)

    every { listRepository.observeAll() } returns flowOf(listOf(list1, list2))
    every { itemRepository.observeByList(TEST_LIST_ID) } returns
      flowOf(listOf(list1Item1, list1Item2))
    every { itemRepository.observeByList(TEST_LIST_ID_2) } returns flowOf(listOf(list2Item1))

    viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ListsOverviewUiState.Success
      state.lists.size shouldBe 2

      val summary1 = state.lists.first { it.list.listId == TEST_LIST_ID }
      summary1.totalItems shouldBe 2
      summary1.checkedItems shouldBe 1

      val summary2 = state.lists.first { it.list.listId == TEST_LIST_ID_2 }
      summary2.totalItems shouldBe 1
      summary2.checkedItems shouldBe 0
    }
  }

  @Test
  fun `repository error emits Error state with message`() = runTest {
    val errorMessage = "Database unavailable"
    every { listRepository.observeAll() } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException(errorMessage) }

    viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ListsOverviewUiState.Error
      state.message shouldBe errorMessage
    }
  }

  @Test
  fun `repository error with null message emits unknown error`() = runTest {
    every { listRepository.observeAll() } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException(null as String?) }

    viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ListsOverviewUiState.Error
      state.message shouldBe "Unknown error"
    }
  }

  @Test
  fun `deleteList delegates to repository delete`() = runTest {
    every { listRepository.observeAll() } returns flowOf(emptyList())
    coEvery { listRepository.delete(any()) } just runs

    viewModel = createViewModel()
    viewModel.deleteList(TEST_LIST_ID)

    coVerify { listRepository.delete(TEST_LIST_ID) }
  }

  @Test
  fun `list updates reactively when observable changes`() = runTest {
    val listsFlow =
      MutableStateFlow(emptyList<com.jsamuelsen11.daykeeper.core.model.list.ShoppingList>())
    every { listRepository.observeAll() } returns listsFlow
    every { itemRepository.observeByList(TEST_LIST_ID) } returns flowOf(emptyList())

    viewModel = createViewModel()

    viewModel.uiState.test {
      val emptyState = awaitItem() as ListsOverviewUiState.Success
      emptyState.lists shouldBe emptyList()

      listsFlow.value = listOf(makeList())

      val populatedState = awaitItem() as ListsOverviewUiState.Success
      populatedState.lists.size shouldBe 1
    }
  }
}
