package com.jsamuelsen11.daykeeper.feature.tasks.category

import com.jsamuelsen11.daykeeper.core.model.task.TaskCategory

sealed interface CategoryManagementUiState {
  data object Loading : CategoryManagementUiState

  data class Success(val categories: List<CategoryItem>) : CategoryManagementUiState

  data class Error(val message: String) : CategoryManagementUiState
}

data class CategoryItem(val category: TaskCategory, val taskCount: Int)
