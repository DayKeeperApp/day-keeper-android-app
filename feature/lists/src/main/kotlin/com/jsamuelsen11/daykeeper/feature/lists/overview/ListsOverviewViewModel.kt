package com.jsamuelsen11.daykeeper.feature.lists.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListItemRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

class ListsOverviewViewModel(
  private val listRepository: ShoppingListRepository,
  private val itemRepository: ShoppingListItemRepository,
) : ViewModel() {

  @OptIn(ExperimentalCoroutinesApi::class)
  val uiState: StateFlow<ListsOverviewUiState> =
    listRepository
      .observeAll()
      .flatMapLatest { lists ->
        if (lists.isEmpty()) {
          flowOf<ListsOverviewUiState>(ListsOverviewUiState.Success(emptyList()))
        } else {
          val itemFlows = lists.map { list -> itemRepository.observeByList(list.listId) }
          combine(itemFlows) { itemArrays ->
            val summaries =
              lists.mapIndexed { index, list ->
                val items = itemArrays[index]
                ShoppingListSummary(
                  list = list,
                  totalItems = items.size,
                  checkedItems = items.count { it.isChecked },
                )
              }
            ListsOverviewUiState.Success(summaries) as ListsOverviewUiState
          }
        }
      }
      .catch { e -> emit(ListsOverviewUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        ListsOverviewUiState.Loading,
      )

  fun deleteList(listId: String) {
    viewModelScope.launch { listRepository.delete(listId) }
  }
}
