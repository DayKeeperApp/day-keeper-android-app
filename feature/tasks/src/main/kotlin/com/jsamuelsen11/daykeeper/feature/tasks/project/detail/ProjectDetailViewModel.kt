package com.jsamuelsen11.daykeeper.feature.tasks.project.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.task.ProjectStatus
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import com.jsamuelsen11.daykeeper.feature.tasks.list.TaskListItem
import com.jsamuelsen11.daykeeper.feature.tasks.navigation.ProjectDetailRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the project detail screen.
 *
 * Combines project, task, and category flows into a single [ProjectDetailUiState] stream. Exposes
 * actions for toggling task completion, archiving a project, and deleting a project.
 */
public class ProjectDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val projectRepository: ProjectRepository,
  private val taskRepository: TaskRepository,
  private val taskCategoryRepository: TaskCategoryRepository,
) : ViewModel() {

  private val projectId: String = savedStateHandle.toRoute<ProjectDetailRoute>().projectId

  /** The current UI state, updated reactively from the underlying data flows. */
  public val uiState: StateFlow<ProjectDetailUiState> =
    combine(
        projectRepository.observeById(projectId),
        taskRepository.observeByProject(projectId),
        taskCategoryRepository.observeAll(),
      ) { project, tasks, categories ->
        if (project == null) {
          ProjectDetailUiState.Error(message = "Project not found")
        } else {
          val categoryMap = categories.associateBy { it.categoryId }
          val activeTasks = tasks.filter { it.deletedAt == null }
          val taskItems =
            activeTasks.map { task ->
              val category = task.categoryId?.let { id -> categoryMap[id] }
              TaskListItem(
                task = task,
                projectName = project.name,
                categoryName = category?.name,
                categoryColor = category?.color,
              )
            }
          val completedCount = taskItems.count { it.task.status == TaskStatus.DONE }
          ProjectDetailUiState.Success(
            project = project,
            tasks = taskItems,
            completedCount = completedCount,
            totalCount = taskItems.size,
          )
        }
      }
      .catch { e -> emit(ProjectDetailUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = ProjectDetailUiState.Loading,
      )

  /**
   * Toggles the completion state of a task.
   *
   * If the task is currently [TaskStatus.DONE] it is moved back to [TaskStatus.TODO]; otherwise it
   * is moved to [TaskStatus.DONE].
   *
   * @param taskId The ID of the task to toggle.
   */
  public fun toggleTaskComplete(taskId: String) {
    viewModelScope.launch {
      val task = taskRepository.getById(taskId) ?: return@launch
      val updatedStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
      taskRepository.upsert(
        task.copy(status = updatedStatus, updatedAt = System.currentTimeMillis())
      )
    }
  }

  /** Permanently deletes this project from the repository. */
  public fun deleteProject() {
    viewModelScope.launch { projectRepository.delete(projectId) }
  }

  /**
   * Archives this project by setting its status to [ProjectStatus.ARCHIVED].
   *
   * No-ops when the project is not currently loaded.
   */
  public fun archiveProject() {
    viewModelScope.launch {
      val project = projectRepository.getById(projectId) ?: return@launch
      projectRepository.upsert(
        project.copy(status = ProjectStatus.ARCHIVED, updatedAt = System.currentTimeMillis())
      )
    }
  }

  public companion object {
    /** Timeout before the upstream flow is stopped after the last subscriber disappears. */
    public const val STOP_TIMEOUT_MILLIS: Long = 5_000L
  }
}
