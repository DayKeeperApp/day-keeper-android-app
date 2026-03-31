package com.jsamuelsen11.daykeeper.feature.tasks.detail

import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.model.task.Project
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskCategory

/** UI state for the task detail screen. */
sealed interface TaskDetailUiState {
  /** Initial loading state before data is available. */
  data object Loading : TaskDetailUiState

  /**
   * Successfully loaded state containing the task and its related entities.
   *
   * @property task The task being displayed.
   * @property project The project this task belongs to, or null if unassigned.
   * @property category The category assigned to this task, or null if uncategorized.
   * @property attachments Attachments associated with this task.
   */
  data class Success(
    val task: Task,
    val project: Project?,
    val category: TaskCategory?,
    val attachments: List<AttachmentUiItem> = emptyList(),
  ) : TaskDetailUiState

  /**
   * Error state when the task cannot be loaded.
   *
   * @property message A human-readable description of the error.
   */
  data class Error(val message: String) : TaskDetailUiState
}
