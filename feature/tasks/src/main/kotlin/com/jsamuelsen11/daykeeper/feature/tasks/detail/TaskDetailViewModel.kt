package com.jsamuelsen11.daykeeper.feature.tasks.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

/**
 * ViewModel for the task detail screen.
 *
 * Observes a single task by ID (extracted from [SavedStateHandle]) and resolves its associated
 * [com.jsamuelsen11.daykeeper.core.model.task.Project] and
 * [com.jsamuelsen11.daykeeper.core.model.task.TaskCategory] reactively. Exposes [uiState] as a
 * [StateFlow] of [TaskDetailUiState].
 *
 * @param savedStateHandle Navigation back-stack handle; must contain a `taskId` key.
 * @param taskRepository Source of truth for task data.
 * @param projectRepository Source of truth for project data.
 * @param taskCategoryRepository Source of truth for task-category data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val taskRepository: TaskRepository,
  private val projectRepository: ProjectRepository,
  private val taskCategoryRepository: TaskCategoryRepository,
) : ViewModel() {

  private val taskId: String = checkNotNull(savedStateHandle["taskId"])

  /** The reactive UI state for this screen. Starts as [TaskDetailUiState.Loading]. */
  val uiState: StateFlow<TaskDetailUiState> =
    taskRepository
      .observeById(taskId)
      .flatMapLatest { task ->
        if (task == null) {
          flowOf(TaskDetailUiState.Error("Task not found"))
        } else {
          val taskProjectId = task.projectId
          val projectFlow =
            if (taskProjectId != null) {
              projectRepository.observeById(taskProjectId)
            } else {
              flowOf(null)
            }

          val categoryFlow =
            taskCategoryRepository.observeAll().map { categories ->
              categories.find { it.categoryId == task.categoryId }
            }

          combine(projectFlow, categoryFlow) { project, category ->
            TaskDetailUiState.Success(task = task, project = project, category = category)
          }
        }
      }
      .catch { e -> emit(TaskDetailUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        TaskDetailUiState.Loading,
      )

  /**
   * Toggles the task completion status between [TaskStatus.TODO] and [TaskStatus.DONE]. When called
   * on a task that is [TaskStatus.DONE] the status reverts to [TaskStatus.TODO]; all other statuses
   * are promoted to [TaskStatus.DONE].
   */
  fun toggleComplete() {
    val state = uiState.value as? TaskDetailUiState.Success ?: return
    viewModelScope.launch {
      val task = state.task
      val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
      taskRepository.upsert(task.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
    }
  }

  /** Permanently deletes the current task from the repository. */
  fun deleteTask() {
    viewModelScope.launch { taskRepository.delete(taskId) }
  }
}
