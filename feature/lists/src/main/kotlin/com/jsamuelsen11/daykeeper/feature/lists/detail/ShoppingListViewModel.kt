package com.jsamuelsen11.daykeeper.feature.lists.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListItemRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListRepository
import com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

class ShoppingListViewModel(
  savedStateHandle: SavedStateHandle,
  private val listRepository: ShoppingListRepository,
  private val itemRepository: ShoppingListItemRepository,
) : ViewModel() {

  private val listId: String = checkNotNull(savedStateHandle["listId"])
  private val checkedExpanded = MutableStateFlow(false)
  private val editingItem = MutableStateFlow<ShoppingListItem?>(null)

  val uiState: StateFlow<ShoppingListUiState> =
    combine(
        listRepository.observeById(listId),
        itemRepository.observeByList(listId),
        checkedExpanded,
        editingItem,
      ) { list, items, expanded, editing ->
        if (list == null) {
          ShoppingListUiState.Error("List not found")
        } else {
          ShoppingListUiState.Success(
            list = list,
            uncheckedItems = items.filter { !it.isChecked },
            checkedItems = items.filter { it.isChecked },
            checkedExpanded = expanded,
            editingItem = editing,
          )
        }
      }
      .catch { e -> emit(ShoppingListUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        ShoppingListUiState.Loading,
      )

  fun addItem(name: String) {
    if (name.isBlank()) return
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val currentItems = (uiState.value as? ShoppingListUiState.Success)?.uncheckedItems
      val maxSortOrder = currentItems?.maxOfOrNull { it.sortOrder } ?: 0
      val item =
        ShoppingListItem(
          itemId = UUID.randomUUID().toString(),
          listId = listId,
          name = name.trim(),
          isChecked = false,
          sortOrder = maxSortOrder + 1,
          createdAt = now,
          updatedAt = now,
        )
      itemRepository.upsert(item)
    }
  }

  fun toggleItem(itemId: String, isChecked: Boolean) {
    viewModelScope.launch { itemRepository.toggleChecked(itemId, isChecked) }
  }

  fun deleteItem(itemId: String) {
    viewModelScope.launch { itemRepository.delete(itemId) }
  }

  fun clearChecked() {
    viewModelScope.launch {
      val state = uiState.value as? ShoppingListUiState.Success ?: return@launch
      state.checkedItems.forEach { itemRepository.delete(it.itemId) }
    }
  }

  fun toggleCheckedExpanded() {
    checkedExpanded.value = !checkedExpanded.value
  }

  fun editItem(item: ShoppingListItem?) {
    editingItem.value = item
  }

  fun updateItem(item: ShoppingListItem) {
    viewModelScope.launch {
      itemRepository.upsert(item.copy(updatedAt = System.currentTimeMillis()))
      editingItem.value = null
    }
  }

  fun reorderItems(fromIndex: Int, toIndex: Int) {
    viewModelScope.launch {
      val state = uiState.value as? ShoppingListUiState.Success ?: return@launch
      val items = state.uncheckedItems.toMutableList()
      val movedItem = items.removeAt(fromIndex)
      items.add(toIndex, movedItem)
      val now = System.currentTimeMillis()
      val updated =
        items.mapIndexed { index, item -> item.copy(sortOrder = index, updatedAt = now) }
      itemRepository.upsertAll(updated)
    }
  }
}
