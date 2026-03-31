package com.jsamuelsen11.daykeeper.feature.tasks.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperFloatingActionButton
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.component.SwipeableListItem
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.tasks.component.TaskCard
import com.jsamuelsen11.daykeeper.feature.tasks.component.TaskFilterBar
import org.koin.compose.viewmodel.koinViewModel

/** Groups task interaction callbacks to reduce parameter count in list composables. */
private data class TaskListCallbacks(
  val onTaskClick: (String) -> Unit,
  val onProjectClick: (String) -> Unit,
  val onToggleComplete: (String) -> Unit,
  val onDeleteTask: (String) -> Unit,
)

private val ListContentPadding = 16.dp
private val ListItemSpacing = 6.dp
private val SpeedDialSpacing = 8.dp
private val SpeedDialIconSize = 20.dp
private val CompletedHeaderPadding = 16.dp
private val CompletedHeaderVerticalPadding = 8.dp

/**
 * Root composable for the task list screen.
 *
 * Renders a [Scaffold] with a top app bar, a tab row that switches between [ViewMode.ALL_TASKS] and
 * [ViewMode.BY_PROJECT], a [TaskFilterBar], and a lazy list of [TaskCard]s. A speed-dial FAB
 * provides shortcuts to create tasks and projects. Swiping a task card left triggers a delete
 * confirmation dialog.
 *
 * @param onTaskClick Invoked with the task ID when the user taps a task card.
 * @param onCreateTask Invoked when the user taps the "New task" FAB action.
 * @param onCreateProject Invoked when the user taps the "New project" FAB action.
 * @param onProjectClick Invoked with the project ID when the user taps a project header.
 * @param modifier Optional modifier applied to the [Scaffold].
 * @param viewModel The screen's [TaskListViewModel]; defaults to Koin-provided instance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
  onTaskClick: (String) -> Unit,
  onCreateTask: () -> Unit,
  onCreateProject: () -> Unit,
  onProjectClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: TaskListViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var deleteTarget by remember { mutableStateOf<String?>(null) }
  var speedDialExpanded by remember { mutableStateOf(false) }

  DeleteTaskDialog(
    deleteTarget = deleteTarget,
    onConfirm = viewModel::deleteTask,
    onDismiss = { deleteTarget = null },
  )

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = "Tasks") },
    floatingActionButton = {
      TaskSpeedDial(
        expanded = speedDialExpanded,
        onToggle = { speedDialExpanded = !speedDialExpanded },
        onCreateTask = {
          speedDialExpanded = false
          onCreateTask()
        },
        onCreateProject = {
          speedDialExpanded = false
          onCreateProject()
        },
      )
    },
  ) { innerPadding ->
    val isRefreshing = (uiState as? TaskListUiState.Success)?.isRefreshing ?: false
    PullToRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = viewModel::onRefresh,
      modifier = Modifier.padding(innerPadding),
    ) {
      TaskListContent(
        uiState = uiState,
        callbacks =
          TaskListCallbacks(
            onTaskClick = onTaskClick,
            onProjectClick = onProjectClick,
            onToggleComplete = viewModel::toggleComplete,
            onDeleteTask = { deleteTarget = it },
          ),
        onViewModeChange = viewModel::setViewMode,
        onStatusFilterChanged = viewModel::setStatusFilter,
        onPriorityFilterChanged = viewModel::setPriorityFilter,
        onCategoryFilterChanged = viewModel::setCategoryFilter,
        onSortOrderChanged = viewModel::setSortOrder,
      )
    }
  }
}

@Composable
private fun DeleteTaskDialog(
  deleteTarget: String?,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (deleteTarget != null) {
    ConfirmationDialog(
      title = "Delete task?",
      body = "This action cannot be undone.",
      icon = DayKeeperIcons.Delete,
      confirmLabel = "Delete",
      dismissLabel = "Cancel",
      onConfirm = {
        onConfirm(deleteTarget)
        onDismiss()
      },
      onDismiss = onDismiss,
      modifier = modifier,
    )
  }
}

@Composable
private fun TaskSpeedDial(
  expanded: Boolean,
  onToggle: () -> Unit,
  onCreateTask: () -> Unit,
  onCreateProject: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.End,
    verticalArrangement = Arrangement.spacedBy(SpeedDialSpacing),
  ) {
    AnimatedVisibility(visible = expanded) {
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(SpeedDialSpacing),
      ) {
        SmallFloatingActionButton(onClick = onCreateProject) {
          Icon(
            imageVector = DayKeeperIcons.Project,
            contentDescription = "New project",
            modifier = Modifier.size(SpeedDialIconSize),
          )
        }
        SmallFloatingActionButton(onClick = onCreateTask) {
          Icon(
            imageVector = DayKeeperIcons.Task,
            contentDescription = "New task",
            modifier = Modifier.size(SpeedDialIconSize),
          )
        }
      }
    }
    DayKeeperFloatingActionButton(
      onClick = onToggle,
      icon = if (expanded) DayKeeperIcons.Close else DayKeeperIcons.Add,
      contentDescription = if (expanded) "Close actions" else "Create",
    )
  }
}

@Composable
private fun TaskListContent(
  uiState: TaskListUiState,
  callbacks: TaskListCallbacks,
  onViewModeChange: (ViewMode) -> Unit,
  onStatusFilterChanged: (Set<com.jsamuelsen11.daykeeper.core.model.task.TaskStatus>) -> Unit,
  onPriorityFilterChanged: (Set<com.jsamuelsen11.daykeeper.core.model.task.Priority>) -> Unit,
  onCategoryFilterChanged: (String?) -> Unit,
  onSortOrderChanged: (SortOrder) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    is TaskListUiState.Loading -> LoadingIndicator(modifier = modifier.fillMaxSize())
    is TaskListUiState.Error ->
      EmptyStateView(
        icon = DayKeeperIcons.Tasks,
        title = "Something went wrong",
        body = uiState.message,
        modifier = modifier,
      )
    is TaskListUiState.Success ->
      TaskListSuccessContent(
        uiState = uiState,
        callbacks = callbacks,
        onViewModeChange = onViewModeChange,
        onStatusFilterChanged = onStatusFilterChanged,
        onPriorityFilterChanged = onPriorityFilterChanged,
        onCategoryFilterChanged = onCategoryFilterChanged,
        onSortOrderChanged = onSortOrderChanged,
        modifier = modifier,
      )
  }
}

@Composable
private fun TaskListSuccessContent(
  uiState: TaskListUiState.Success,
  callbacks: TaskListCallbacks,
  onViewModeChange: (ViewMode) -> Unit,
  onStatusFilterChanged: (Set<com.jsamuelsen11.daykeeper.core.model.task.TaskStatus>) -> Unit,
  onPriorityFilterChanged: (Set<com.jsamuelsen11.daykeeper.core.model.task.Priority>) -> Unit,
  onCategoryFilterChanged: (String?) -> Unit,
  onSortOrderChanged: (SortOrder) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize()) {
    ViewModeTabRow(selected = uiState.viewMode, onSelect = onViewModeChange)

    TaskFilterBar(
      filters = uiState.filters,
      categories = uiState.categories,
      onStatusFilterChanged = onStatusFilterChanged,
      onPriorityFilterChanged = onPriorityFilterChanged,
      onCategoryFilterChanged = onCategoryFilterChanged,
      onSortOrderChanged = onSortOrderChanged,
    )

    HorizontalDivider()

    when (uiState.viewMode) {
      ViewMode.ALL_TASKS ->
        AllTasksList(
          items = uiState.items,
          completedItems = uiState.completedItems,
          onTaskClick = callbacks.onTaskClick,
          onToggleComplete = callbacks.onToggleComplete,
          onDeleteTask = callbacks.onDeleteTask,
        )
      ViewMode.BY_PROJECT ->
        ByProjectList(
          groups = uiState.projectGroups,
          standaloneItems = uiState.items.filter { it.task.projectId == null },
          completedItems = uiState.completedItems,
          callbacks = callbacks,
        )
    }
  }
}

@Composable
private fun ViewModeTabRow(
  selected: ViewMode,
  onSelect: (ViewMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  val tabs = ViewMode.entries
  @OptIn(ExperimentalMaterial3Api::class)
  PrimaryTabRow(selectedTabIndex = tabs.indexOf(selected), modifier = modifier) {
    tabs.forEach { mode ->
      Tab(
        selected = mode == selected,
        onClick = { onSelect(mode) },
        text = {
          Text(
            text =
              when (mode) {
                ViewMode.ALL_TASKS -> "All Tasks"
                ViewMode.BY_PROJECT -> "By Project"
              }
          )
        },
      )
    }
  }
}

@Composable
private fun AllTasksList(
  items: List<TaskListItem>,
  completedItems: List<TaskListItem>,
  onTaskClick: (String) -> Unit,
  onToggleComplete: (String) -> Unit,
  onDeleteTask: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (items.isEmpty() && completedItems.isEmpty()) {
    EmptyStateView(
      icon = DayKeeperIcons.Tasks,
      title = "No tasks yet",
      body = "Tap + to create your first task.",
      modifier = modifier,
    )
    return
  }

  TaskLazyColumn(
    items = items,
    completedItems = completedItems,
    onTaskClick = onTaskClick,
    onToggleComplete = onToggleComplete,
    onDeleteTask = onDeleteTask,
    modifier = modifier,
  )
}

@Composable
private fun ByProjectList(
  groups: List<ProjectGroup>,
  standaloneItems: List<TaskListItem>,
  completedItems: List<TaskListItem>,
  callbacks: TaskListCallbacks,
  modifier: Modifier = Modifier,
) {
  if (groups.isEmpty() && standaloneItems.isEmpty() && completedItems.isEmpty()) {
    EmptyStateView(
      icon = DayKeeperIcons.Tasks,
      title = "No tasks yet",
      body = "Tap + to create your first task.",
      modifier = modifier,
    )
    return
  }

  var completedExpanded by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(ListContentPadding),
    verticalArrangement = Arrangement.spacedBy(ListItemSpacing),
  ) {
    groups.forEach { group ->
      item(key = "project_${group.project.projectId}") {
        ProjectGroupHeader(
          group = group,
          onClick = { callbacks.onProjectClick(group.project.projectId) },
        )
      }
      items(items = group.tasks, key = { "task_${it.task.taskId}" }) { item ->
        SwipeableTaskCard(item = item, callbacks = callbacks)
      }
    }

    if (standaloneItems.isNotEmpty()) {
      item(key = "standalone_header") {
        Text(
          text = "No Project",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = ListItemSpacing),
        )
      }
      items(items = standaloneItems, key = { "task_${it.task.taskId}" }) { item ->
        SwipeableTaskCard(item = item, callbacks = callbacks)
      }
    }

    if (completedItems.isNotEmpty()) {
      item(key = "completed_header") {
        CompletedSectionHeader(
          count = completedItems.size,
          expanded = completedExpanded,
          onToggle = { completedExpanded = !completedExpanded },
        )
      }
      if (completedExpanded) {
        items(items = completedItems, key = { "done_${it.task.taskId}" }) { item ->
          SwipeableTaskCard(item = item, callbacks = callbacks)
        }
      }
    }
  }
}

@Composable
private fun SwipeableTaskCard(item: TaskListItem, callbacks: TaskListCallbacks) {
  SwipeableListItem(onDelete = { callbacks.onDeleteTask(item.task.taskId) }) {
    TaskCard(
      item = item,
      onToggleComplete = { callbacks.onToggleComplete(item.task.taskId) },
      onClick = { callbacks.onTaskClick(item.task.taskId) },
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun TaskLazyColumn(
  items: List<TaskListItem>,
  completedItems: List<TaskListItem>,
  onTaskClick: (String) -> Unit,
  onToggleComplete: (String) -> Unit,
  onDeleteTask: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val callbacks =
    TaskListCallbacks(
      onTaskClick = onTaskClick,
      onProjectClick = {},
      onToggleComplete = onToggleComplete,
      onDeleteTask = onDeleteTask,
    )
  var completedExpanded by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(ListContentPadding),
    verticalArrangement = Arrangement.spacedBy(ListItemSpacing),
  ) {
    items(items = items, key = { "task_${it.task.taskId}" }) { item ->
      SwipeableTaskCard(item = item, callbacks = callbacks)
    }

    if (completedItems.isNotEmpty()) {
      item(key = "completed_header") {
        CompletedSectionHeader(
          count = completedItems.size,
          expanded = completedExpanded,
          onToggle = { completedExpanded = !completedExpanded },
        )
      }
      if (completedExpanded) {
        items(items = completedItems, key = { "done_${it.task.taskId}" }) { item ->
          SwipeableTaskCard(item = item, callbacks = callbacks)
        }
      }
    }
  }
}

@Composable
private fun ProjectGroupHeader(
  group: ProjectGroup,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = CompletedHeaderPadding, vertical = CompletedHeaderVerticalPadding)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpeedDialSpacing),
      ) {
        Icon(
          imageVector = DayKeeperIcons.Project,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = group.project.name, style = MaterialTheme.typography.titleSmall)
      }
      Icon(
        imageVector = DayKeeperIcons.ChevronRight,
        contentDescription = "Open project",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    if (group.completedCount > 0) {
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${group.completedCount} completed",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun CompletedSectionHeader(
  count: Int,
  expanded: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HorizontalDivider()
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onToggle)
        .padding(horizontal = CompletedHeaderPadding, vertical = CompletedHeaderVerticalPadding)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(SpeedDialSpacing),
    ) {
      Icon(
        imageVector = if (expanded) DayKeeperIcons.ExpandLess else DayKeeperIcons.ExpandMore,
        contentDescription = if (expanded) "Collapse completed" else "Expand completed",
      )
      Text(
        text = "Completed ($count)",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
