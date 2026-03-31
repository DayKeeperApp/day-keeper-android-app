package com.jsamuelsen11.daykeeper.feature.tasks.list

import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.Project
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus

/** Top-level UI state for the task list screen. */
sealed interface TaskListUiState {
  /** Data is being loaded for the first time. */
  data object Loading : TaskListUiState

  /** Tasks have been loaded and are ready to display. */
  data class Success(
    val items: List<TaskListItem>,
    val projectGroups: List<ProjectGroup>,
    val completedItems: List<TaskListItem>,
    val viewMode: ViewMode,
    val filters: TaskFilters,
    val categories: List<CategoryOption>,
    val isRefreshing: Boolean = false,
  ) : TaskListUiState

  /** An unrecoverable error occurred while loading tasks. */
  data class Error(val message: String) : TaskListUiState
}

/**
 * A single task enriched with display-ready project and category metadata.
 *
 * @param task The underlying task model.
 * @param projectName Display name of the task's project, or null if the task is unassigned.
 * @param categoryName Display name of the task's category, or null if no category is set.
 * @param categoryColor Hex color string of the category, or null if none.
 */
data class TaskListItem(
  val task: Task,
  val projectName: String?,
  val categoryName: String?,
  val categoryColor: String?,
)

/**
 * A project header with its associated tasks, used in [ViewMode.BY_PROJECT] layout.
 *
 * @param project The project model.
 * @param tasks Non-completed tasks belonging to this project.
 * @param completedCount Number of completed tasks within this project.
 */
data class ProjectGroup(
  val project: Project,
  val tasks: List<TaskListItem>,
  val completedCount: Int,
)

/**
 * A category option shown inside the filter bar's category selector.
 *
 * @param categoryId Stable identifier for selection tracking.
 * @param name Human-readable category name.
 * @param color Optional hex color string for the category dot.
 */
data class CategoryOption(val categoryId: String, val name: String, val color: String?)

/**
 * The complete set of active filters applied to the task list.
 *
 * An empty [statuses] or [priorities] set means "show all".
 *
 * @param statuses Active status filter set. Empty means no status filter.
 * @param priorities Active priority filter set. Empty means no priority filter.
 * @param categoryId Active category filter, or null for all categories.
 * @param sortOrder Current sort order applied to the list.
 */
data class TaskFilters(
  val statuses: Set<TaskStatus> = emptySet(),
  val priorities: Set<Priority> = emptySet(),
  val categoryId: String? = null,
  val sortOrder: SortOrder = SortOrder.DUE_DATE,
)

/** Controls whether tasks are shown as a flat list or grouped under their projects. */
enum class ViewMode {
  ALL_TASKS,
  BY_PROJECT,
}

/** The sort order applied to tasks within the list. */
enum class SortOrder {
  DUE_DATE,
  PRIORITY,
  RECENTLY_ADDED,
}
