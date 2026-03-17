package com.jsamuelsen11.daykeeper.feature.tasks.project.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperFloatingActionButton
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.component.PriorityIndicator
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.tasks.list.TaskListItem
import org.koin.compose.viewmodel.koinViewModel

private val TaskRowHorizontalPadding = 16.dp
private val TaskRowVerticalPadding = 12.dp
private val TaskRowSpacing = 4.dp
private val ProgressSectionPadding = 16.dp
private val ProgressBarBottomPadding = 4.dp

/**
 * Displays a project's details, progress, and associated tasks.
 *
 * @param onNavigateBack Called when the user presses the back button.
 * @param onEditProject Called when the user selects the edit action.
 * @param onTaskClick Called with the task ID when a task row is tapped.
 * @param onCreateTask Called when the FAB is pressed to create a new task.
 * @param modifier Optional [Modifier] applied to the root scaffold.
 * @param viewModel The [ProjectDetailViewModel] provided by Koin.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ProjectDetailScreen(
  onNavigateBack: () -> Unit,
  onEditProject: () -> Unit,
  onTaskClick: (String) -> Unit,
  onCreateTask: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProjectDetailViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  var showOverflowMenu by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var showArchiveDialog by remember { mutableStateOf(false) }

  ProjectDetailDialogs(
    showDeleteDialog = showDeleteDialog,
    showArchiveDialog = showArchiveDialog,
    onDeleteConfirm = {
      showDeleteDialog = false
      viewModel.deleteProject()
      onNavigateBack()
    },
    onDeleteDismiss = { showDeleteDialog = false },
    onArchiveConfirm = {
      showArchiveDialog = false
      viewModel.archiveProject()
      onNavigateBack()
    },
    onArchiveDismiss = { showArchiveDialog = false },
  )

  Scaffold(
    modifier = modifier,
    topBar = {
      ProjectDetailTopBar(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onEditProject = onEditProject,
        showOverflowMenu = showOverflowMenu,
        onToggleOverflowMenu = { showOverflowMenu = !showOverflowMenu },
        onDismissOverflowMenu = { showOverflowMenu = false },
        onArchive = { showArchiveDialog = true },
        onDelete = { showDeleteDialog = true },
      )
    },
    floatingActionButton = {
      DayKeeperFloatingActionButton(
        onClick = onCreateTask,
        icon = DayKeeperIcons.Add,
        contentDescription = "Add task",
        text = "Add Task",
      )
    },
  ) { innerPadding ->
    ProjectDetailStateContent(
      uiState = uiState,
      onTaskClick = onTaskClick,
      onToggleTaskComplete = viewModel::toggleTaskComplete,
      modifier = Modifier.padding(innerPadding),
    )
  }
}

@Composable
private fun ProjectDetailStateContent(
  uiState: ProjectDetailUiState,
  onTaskClick: (String) -> Unit,
  onToggleTaskComplete: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (val state = uiState) {
    is ProjectDetailUiState.Loading -> LoadingIndicator(modifier = modifier)
    is ProjectDetailUiState.Error ->
      EmptyStateView(
        icon = DayKeeperIcons.Info,
        title = "Something went wrong",
        body = state.message,
        modifier = modifier,
      )
    is ProjectDetailUiState.Success ->
      ProjectDetailContent(
        state = state,
        onTaskClick = onTaskClick,
        onToggleTaskComplete = onToggleTaskComplete,
        modifier = modifier,
      )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectDetailTopBar(
  uiState: ProjectDetailUiState,
  onNavigateBack: () -> Unit,
  onEditProject: () -> Unit,
  showOverflowMenu: Boolean,
  onToggleOverflowMenu: () -> Unit,
  onDismissOverflowMenu: () -> Unit,
  onArchive: () -> Unit,
  onDelete: () -> Unit,
) {
  DayKeeperTopAppBar(
    title = if (uiState is ProjectDetailUiState.Success) uiState.project.name else "Project",
    onNavigationClick = onNavigateBack,
    actions = {
      IconButton(onClick = onEditProject) {
        Icon(imageVector = DayKeeperIcons.Edit, contentDescription = "Edit project")
      }
      IconButton(onClick = onToggleOverflowMenu) {
        Icon(imageVector = DayKeeperIcons.MoreVert, contentDescription = "More options")
      }
      DropdownMenu(expanded = showOverflowMenu, onDismissRequest = onDismissOverflowMenu) {
        DropdownMenuItem(
          text = { Text("Archive") },
          onClick = {
            onDismissOverflowMenu()
            onArchive()
          },
        )
        DropdownMenuItem(
          text = { Text("Delete") },
          onClick = {
            onDismissOverflowMenu()
            onDelete()
          },
        )
      }
    },
  )
}

@Composable
private fun ProjectDetailDialogs(
  showDeleteDialog: Boolean,
  showArchiveDialog: Boolean,
  onDeleteConfirm: () -> Unit,
  onDeleteDismiss: () -> Unit,
  onArchiveConfirm: () -> Unit,
  onArchiveDismiss: () -> Unit,
) {
  if (showDeleteDialog) {
    ConfirmationDialog(
      title = "Delete project?",
      body = "All tasks in this project will also be deleted. This cannot be undone.",
      icon = DayKeeperIcons.Delete,
      confirmLabel = "Delete",
      onConfirm = onDeleteConfirm,
      onDismiss = onDeleteDismiss,
    )
  }

  if (showArchiveDialog) {
    ConfirmationDialog(
      title = "Archive project?",
      body = "The project will be archived and hidden from the active view.",
      confirmLabel = "Archive",
      onConfirm = onArchiveConfirm,
      onDismiss = onArchiveDismiss,
    )
  }
}

@Composable
private fun ProjectDetailContent(
  state: ProjectDetailUiState.Success,
  onTaskClick: (String) -> Unit,
  onToggleTaskComplete: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val progressFraction =
    if (state.totalCount == 0) 0f else state.completedCount.toFloat() / state.totalCount.toFloat()

  LazyColumn(modifier = modifier.fillMaxSize()) {
    item {
      Column(modifier = Modifier.padding(ProgressSectionPadding)) {
        LinearProgressIndicator(progress = { progressFraction }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(ProgressBarBottomPadding))
        Text(
          text = "${state.completedCount} of ${state.totalCount} tasks completed",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    if (state.tasks.isEmpty()) {
      item {
        EmptyStateView(
          icon = DayKeeperIcons.Task,
          title = "No tasks yet",
          body = "Tap \"Add Task\" to create the first task in this project.",
        )
      }
    } else {
      items(items = state.tasks, key = { it.task.taskId }) { item ->
        ProjectTaskRow(
          item = item,
          onTaskClick = onTaskClick,
          onToggleComplete = onToggleTaskComplete,
        )
      }
    }
  }
}

@Composable
private fun ProjectTaskRow(
  item: TaskListItem,
  onTaskClick: (String) -> Unit,
  onToggleComplete: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable { onTaskClick(item.task.taskId) }
        .padding(horizontal = TaskRowHorizontalPadding, vertical = TaskRowVerticalPadding),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(TaskRowSpacing),
  ) {
    Checkbox(
      checked = item.task.status == TaskStatus.DONE,
      onCheckedChange = { onToggleComplete(item.task.taskId) },
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = item.task.title,
        style = MaterialTheme.typography.bodyLarge,
        color =
          if (item.task.status == TaskStatus.DONE) {
            MaterialTheme.colorScheme.onSurfaceVariant
          } else {
            MaterialTheme.colorScheme.onSurface
          },
      )
      item.task.dueDate?.let { dueDate ->
        Text(
          text = dueDate,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    PriorityIndicator(priority = item.task.priority)
  }
}
