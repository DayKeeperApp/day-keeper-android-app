package com.jsamuelsen11.daykeeper.feature.lists.detail

import com.jsamuelsen11.daykeeper.core.model.list.ShoppingList
import com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem

sealed interface ShoppingListUiState {
  data object Loading : ShoppingListUiState

  data class Success(
    val list: ShoppingList,
    val uncheckedItems: List<ShoppingListItem>,
    val checkedItems: List<ShoppingListItem>,
    val checkedExpanded: Boolean = false,
    val editingItem: ShoppingListItem? = null,
  ) : ShoppingListUiState

  data class Error(val message: String) : ShoppingListUiState
}
