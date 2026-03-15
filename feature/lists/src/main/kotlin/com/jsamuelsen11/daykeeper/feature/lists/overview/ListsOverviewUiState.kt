package com.jsamuelsen11.daykeeper.feature.lists.overview

import com.jsamuelsen11.daykeeper.core.model.list.ShoppingList

sealed interface ListsOverviewUiState {
  data object Loading : ListsOverviewUiState

  data class Success(val lists: List<ShoppingListSummary>) : ListsOverviewUiState

  data class Error(val message: String) : ListsOverviewUiState
}

data class ShoppingListSummary(val list: ShoppingList, val totalItems: Int, val checkedItems: Int)
