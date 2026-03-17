package com.jsamuelsen11.daykeeper.feature.tasks.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.task.TaskCategory
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val DEFAULT_SPACE_ID = "default-space"

class CategoryManagementViewModel(
  private val taskCategoryRepository: TaskCategoryRepository,
  private val taskRepository: TaskRepository,
) : ViewModel() {

  val uiState: StateFlow<CategoryManagementUiState> =
    combine(taskCategoryRepository.observeAll(), taskRepository.observeBySpace(DEFAULT_SPACE_ID)) {
        categories,
        tasks ->
        val countsByCategory = tasks.groupingBy { it.categoryId }.eachCount()
        val items =
          categories
            .sortedWith(compareByDescending<TaskCategory> { it.isSystem }.thenBy { it.name })
            .map { category ->
              CategoryItem(
                category = category,
                taskCount = countsByCategory[category.categoryId] ?: 0,
              )
            }
        CategoryManagementUiState.Success(items) as CategoryManagementUiState
      }
      .catch { e -> emit(CategoryManagementUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        CategoryManagementUiState.Loading,
      )

  fun createCategory(name: String, color: String?) {
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val category =
        TaskCategory(
          categoryId = UUID.randomUUID().toString(),
          name = name.trim(),
          normalizedName = name.trim().lowercase(),
          isSystem = false,
          color = color,
          createdAt = now,
          updatedAt = now,
        )
      taskCategoryRepository.upsert(category)
    }
  }

  fun updateCategory(categoryId: String, name: String, color: String?) {
    viewModelScope.launch {
      val existing = taskCategoryRepository.getById(categoryId) ?: return@launch
      taskCategoryRepository.upsert(
        existing.copy(
          name = name.trim(),
          normalizedName = name.trim().lowercase(),
          color = color,
          updatedAt = System.currentTimeMillis(),
        )
      )
    }
  }

  fun deleteCategory(categoryId: String) {
    viewModelScope.launch { taskCategoryRepository.delete(categoryId) }
  }
}
