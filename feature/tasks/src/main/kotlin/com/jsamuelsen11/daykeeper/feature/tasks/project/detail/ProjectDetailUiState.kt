package com.jsamuelsen11.daykeeper.feature.tasks.project.detail

import com.jsamuelsen11.daykeeper.core.model.task.Project
import com.jsamuelsen11.daykeeper.feature.tasks.list.TaskListItem

/** Represents the possible UI states for the project detail screen. */
public sealed interface ProjectDetailUiState {
  /** Data is being loaded. */
  public data object Loading : ProjectDetailUiState

  /**
   * Project and tasks have been loaded successfully.
   *
   * @property project The project being displayed.
   * @property tasks The enriched task list items for this project.
   * @property completedCount Number of tasks in a terminal completed state.
   * @property totalCount Total number of non-deleted tasks in this project.
   */
  public data class Success(
    val project: Project,
    val tasks: List<TaskListItem>,
    val completedCount: Int,
    val totalCount: Int,
  ) : ProjectDetailUiState

  /**
   * An error occurred while loading the project.
   *
   * @property message Human-readable error description.
   */
  public data class Error(val message: String) : ProjectDetailUiState
}
