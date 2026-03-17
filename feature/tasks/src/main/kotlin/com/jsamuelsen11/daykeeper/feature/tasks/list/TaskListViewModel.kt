package com.jsamuelsen11.daykeeper.feature.tasks.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.Project
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val DEFAULT_SPACE_ID = "default-space"

private val COMPLETED_STATUSES = setOf(TaskStatus.DONE, TaskStatus.CANCELLED)

class TaskListViewModel(
  private val taskRepository: TaskRepository,
  private val projectRepository: ProjectRepository,
  private val taskCategoryRepository: TaskCategoryRepository,
) : ViewModel() {

  private val viewMode = MutableStateFlow(ViewMode.ALL_TASKS)
  private val statusFilter = MutableStateFlow<Set<TaskStatus>>(emptySet())
  private val priorityFilter = MutableStateFlow<Set<Priority>>(emptySet())
  private val categoryFilter = MutableStateFlow<String?>(null)
  private val sortOrder = MutableStateFlow(SortOrder.DUE_DATE)

  val uiState: StateFlow<TaskListUiState> = run {
    val dataFlow =
      combine(
        taskRepository.observeBySpace(DEFAULT_SPACE_ID),
        projectRepository.observeBySpace(DEFAULT_SPACE_ID),
        taskCategoryRepository.observeAll(),
      ) { tasks, projects, categories ->
        Triple(tasks, projects, categories)
      }
    val filterFlow =
      combine(viewMode, statusFilter, priorityFilter, categoryFilter, sortOrder) {
        mode,
        statuses,
        priorities,
        catId,
        order ->
        FilterState(mode, statuses, priorities, catId, order)
      }
    combine(dataFlow, filterFlow) { (tasks, projects, categories), filters ->
        buildUiState(tasks, projects, categories, filters)
      }
      .catch { e -> emit(TaskListUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        TaskListUiState.Loading,
      )
  }

  fun setViewMode(mode: ViewMode) {
    viewMode.value = mode
  }

  fun setStatusFilter(statuses: Set<TaskStatus>) {
    statusFilter.value = statuses
  }

  fun setPriorityFilter(priorities: Set<Priority>) {
    priorityFilter.value = priorities
  }

  fun setCategoryFilter(catId: String?) {
    categoryFilter.value = catId
  }

  fun setSortOrder(order: SortOrder) {
    sortOrder.value = order
  }

  fun toggleComplete(taskId: String) {
    viewModelScope.launch {
      val task = taskRepository.getById(taskId) ?: return@launch
      val newStatus = if (task.status in COMPLETED_STATUSES) TaskStatus.TODO else TaskStatus.DONE
      taskRepository.upsert(task.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
    }
  }

  fun deleteTask(taskId: String) {
    viewModelScope.launch { taskRepository.delete(taskId) }
  }

  private fun buildUiState(
    tasks: List<Task>,
    projects: List<Project>,
    categories: List<com.jsamuelsen11.daykeeper.core.model.task.TaskCategory>,
    filters: FilterState,
  ): TaskListUiState {
    val projectById = projects.associateBy { it.projectId }
    val categoryById = categories.associateBy { it.categoryId }

    val categoryOptions =
      categories.map { cat -> CategoryOption(cat.categoryId, cat.name, cat.color) }

    val taskFilters =
      TaskFilters(filters.statuses, filters.priorities, filters.categoryId, filters.sortOrder)

    val allItems =
      tasks
        .filter { it.deletedAt == null }
        .map { task ->
          val taskProjectId = task.projectId
          val taskCategoryId = task.categoryId
          TaskListItem(
            task = task,
            projectName = if (taskProjectId != null) projectById[taskProjectId]?.name else null,
            categoryName = if (taskCategoryId != null) categoryById[taskCategoryId]?.name else null,
            categoryColor =
              if (taskCategoryId != null) categoryById[taskCategoryId]?.color else null,
          )
        }

    val (completedItems, activeItems) = allItems.partition { it.task.status in COMPLETED_STATUSES }

    val filteredActive = activeItems.filter { item -> matchesFilters(item.task, taskFilters) }
    val sortedActive = filteredActive.sortedWith(taskComparator(filters.sortOrder))

    val projectGroups =
      if (filters.viewMode == ViewMode.BY_PROJECT) {
        buildProjectGroups(sortedActive, completedItems, projectById)
      } else {
        emptyList()
      }

    return TaskListUiState.Success(
      items = sortedActive,
      projectGroups = projectGroups,
      completedItems = completedItems,
      viewMode = filters.viewMode,
      filters = taskFilters,
      categories = categoryOptions,
    )
  }

  private fun matchesFilters(task: Task, filters: TaskFilters): Boolean =
    (filters.statuses.isEmpty() || task.status in filters.statuses) &&
      (filters.priorities.isEmpty() || task.priority in filters.priorities) &&
      (filters.categoryId == null || task.categoryId == filters.categoryId)

  private fun taskComparator(order: SortOrder): Comparator<TaskListItem> =
    when (order) {
      SortOrder.DUE_DATE ->
        compareBy(nullsLast()) { item: TaskListItem -> item.task.dueAt }
          .thenByDescending { it.task.priority.ordinal }
      SortOrder.PRIORITY ->
        compareByDescending<TaskListItem> { it.task.priority.ordinal }
          .thenBy(nullsLast()) { it.task.dueAt }
      SortOrder.RECENTLY_ADDED -> compareByDescending { it.task.createdAt }
    }

  private fun buildProjectGroups(
    activeItems: List<TaskListItem>,
    completedItems: List<TaskListItem>,
    projectById: Map<String, Project>,
  ): List<ProjectGroup> {
    val activeByProject =
      activeItems.filter { it.task.projectId != null }.groupBy { it.task.projectId!! }

    val completedCountByProject =
      completedItems
        .filter { it.task.projectId != null }
        .groupingBy { it.task.projectId!! }
        .eachCount()

    val allProjectIds =
      (activeByProject.keys + completedCountByProject.keys)
        .toSet()
        .mapNotNull { projectById[it] }
        .sortedBy { it.name }

    return allProjectIds.map { project ->
      ProjectGroup(
        project = project,
        tasks = activeByProject[project.projectId].orEmpty(),
        completedCount = completedCountByProject[project.projectId] ?: 0,
      )
    }
  }
}

private data class FilterState(
  val viewMode: ViewMode,
  val statuses: Set<TaskStatus>,
  val priorities: Set<Priority>,
  val categoryId: String?,
  val sortOrder: SortOrder,
)
