package com.jsamuelsen11.daykeeper.feature.tasks.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.model.attachment.DownloadState
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.model.task.Project
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskCategory
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import com.jsamuelsen11.daykeeper.core.ui.component.AttachmentRow
import com.jsamuelsen11.daykeeper.core.ui.component.CategoryChip
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.component.ImagePreview
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.component.PriorityIndicator
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import org.koin.compose.viewmodel.koinViewModel

private val ContentHorizontalPadding = 16.dp
private val ContentVerticalPadding = 16.dp
private val SectionSpacing = 12.dp
private val ChipRowSpacing = 8.dp
private val MetaRowSpacing = 8.dp
private val MetaIconTextSpacing = 8.dp

/**
 * Root composable for the task detail screen.
 *
 * Observes [TaskDetailViewModel.uiState] and renders the appropriate content for [Loading],
 * [Success], and [Error] states.
 *
 * @param onNavigateBack Called when the user taps the back navigation icon.
 * @param onEditTask Called when the user taps the edit action.
 * @param onProjectClick Called with a project ID when the user taps the project row.
 * @param modifier Optional [Modifier] applied to the root [Scaffold].
 * @param viewModel The [TaskDetailViewModel] provided by Koin; override in tests.
 */
@Composable
fun TaskDetailScreen(
  onNavigateBack: () -> Unit,
  onEditTask: () -> Unit,
  onProjectClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: TaskDetailViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showDeleteDialog by remember { mutableStateOf(false) }
  var menuExpanded by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier,
    topBar = {
      TaskDetailTopBar(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onEditTask = onEditTask,
        menuExpanded = menuExpanded,
        onToggleMenu = { menuExpanded = !menuExpanded },
        onDismissMenu = { menuExpanded = false },
        onRequestDelete = { showDeleteDialog = true },
      )
    },
  ) { innerPadding ->
    TaskDetailStateContent(
      uiState = uiState,
      onToggleComplete = viewModel::toggleComplete,
      onProjectClick = onProjectClick,
      onDownloadAttachment = viewModel::downloadAttachment,
      onDeleteAttachment = viewModel::deleteAttachment,
      innerPadding = innerPadding,
    )
  }

  if (showDeleteDialog) {
    ConfirmationDialog(
      title = "Delete task?",
      body = "This action cannot be undone.",
      icon = DayKeeperIcons.Delete,
      confirmLabel = "Delete",
      dismissLabel = "Cancel",
      onConfirm = {
        showDeleteDialog = false
        viewModel.deleteTask()
        onNavigateBack()
      },
      onDismiss = { showDeleteDialog = false },
    )
  }
}

@Composable
private fun TaskDetailTopBar(
  uiState: TaskDetailUiState,
  onNavigateBack: () -> Unit,
  onEditTask: () -> Unit,
  menuExpanded: Boolean,
  onToggleMenu: () -> Unit,
  onDismissMenu: () -> Unit,
  onRequestDelete: () -> Unit,
) {
  val title = if (uiState is TaskDetailUiState.Success) uiState.task.title else "Task"
  DayKeeperTopAppBar(
    title = title,
    onNavigationClick = onNavigateBack,
    actions = {
      IconButton(onClick = onEditTask) {
        Icon(imageVector = DayKeeperIcons.Edit, contentDescription = "Edit task")
      }
      IconButton(onClick = onToggleMenu) {
        Icon(imageVector = DayKeeperIcons.MoreVert, contentDescription = "More options")
      }
      DropdownMenu(expanded = menuExpanded, onDismissRequest = onDismissMenu) {
        DropdownMenuItem(
          text = { Text("Delete") },
          onClick = {
            onDismissMenu()
            onRequestDelete()
          },
          leadingIcon = { Icon(imageVector = DayKeeperIcons.Delete, contentDescription = null) },
        )
      }
    },
  )
}

@Composable
private fun TaskDetailStateContent(
  uiState: TaskDetailUiState,
  onToggleComplete: () -> Unit,
  onProjectClick: (String) -> Unit,
  onDownloadAttachment: (AttachmentUiItem) -> Unit,
  onDeleteAttachment: (String) -> Unit,
  innerPadding: PaddingValues,
) {
  when (val state = uiState) {
    is TaskDetailUiState.Loading ->
      LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
    is TaskDetailUiState.Error ->
      EmptyStateView(
        icon = DayKeeperIcons.Task,
        title = "Task not found",
        body = state.message,
        modifier = Modifier.padding(innerPadding),
      )
    is TaskDetailUiState.Success ->
      TaskDetailContent(
        state = state,
        onToggleComplete = onToggleComplete,
        onProjectClick = onProjectClick,
        onDownloadAttachment = onDownloadAttachment,
        onDeleteAttachment = onDeleteAttachment,
        modifier = Modifier.padding(innerPadding),
      )
  }
}

private const val MIME_TYPE_IMAGE_PREFIX = "image/"

@Composable
private fun TaskDetailContent(
  state: TaskDetailUiState.Success,
  onToggleComplete: () -> Unit,
  onProjectClick: (String) -> Unit,
  onDownloadAttachment: (AttachmentUiItem) -> Unit,
  onDeleteAttachment: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = ContentHorizontalPadding, vertical = ContentVerticalPadding),
    verticalArrangement = Arrangement.spacedBy(SectionSpacing),
  ) {
    TitleRow(task = state.task, onToggleComplete = onToggleComplete)
    ChipRow(task = state.task, category = state.category)
    val project = state.project
    if (project != null) {
      ProjectRow(project = project, onProjectClick = onProjectClick)
    }
    val dueDate = state.task.dueDate
    val dueAt = state.task.dueAt
    if (dueDate != null || dueAt != null) {
      DueDateRow(task = state.task)
    }
    val recurrenceRule = state.task.recurrenceRule
    if (recurrenceRule != null) {
      RecurrenceRow(rule = recurrenceRule)
    }
    val reminderMinutes = state.task.reminderMinutesBefore
    if (reminderMinutes != null) {
      ReminderRow(minutesBefore = reminderMinutes)
    }
    val description = state.task.description
    if (!description.isNullOrBlank()) {
      DescriptionSection(description = description)
    }
    AttachmentSection(
      attachments = state.attachments,
      onDownload = onDownloadAttachment,
      onDelete = onDeleteAttachment,
    )
  }
}

@Composable
private fun AttachmentSection(
  attachments: List<AttachmentUiItem>,
  onDownload: (AttachmentUiItem) -> Unit,
  onDelete: (String) -> Unit,
) {
  var previewItem by remember { mutableStateOf<AttachmentUiItem?>(null) }

  val currentPreviewItem = previewItem
  if (currentPreviewItem != null) {
    val imageModel: Any? =
      when (val ds = currentPreviewItem.downloadState) {
        is DownloadState.Downloaded -> ds.localPath
        else -> currentPreviewItem.remoteUrl
      }
    ImagePreview(
      imageModel = imageModel,
      fileName = currentPreviewItem.fileName,
      onDismiss = { previewItem = null },
    )
  }

  AttachmentRow(
    attachments = attachments,
    onAddClick = {},
    onAttachmentClick = { item ->
      if (item.mimeType.startsWith(MIME_TYPE_IMAGE_PREFIX)) previewItem = item else onDownload(item)
    },
    onDeleteAttachment = onDelete,
  )
}

@Composable
private fun TitleRow(task: Task, onToggleComplete: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(MetaIconTextSpacing),
  ) {
    Checkbox(checked = task.status == TaskStatus.DONE, onCheckedChange = { onToggleComplete() })
    Text(
      text = task.title,
      style = MaterialTheme.typography.headlineSmall,
      textDecoration =
        if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else TextDecoration.None,
      color =
        if (task.status == TaskStatus.DONE) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun ChipRow(task: Task, category: TaskCategory?, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(ChipRowSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    AssistChip(onClick = {}, label = { Text(statusLabel(task.status)) })
    PriorityIndicator(priority = task.priority, showLabel = true)
    if (category != null) {
      val chipColor = category.color?.let { parseHexColor(it) }
      CategoryChip(name = category.name, color = chipColor)
    }
  }
}

@Composable
private fun ProjectRow(
  project: Project,
  onProjectClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable { onProjectClick(project.projectId) }
        .padding(vertical = MetaRowSpacing),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(MetaIconTextSpacing),
  ) {
    Icon(
      imageVector = DayKeeperIcons.Project,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = project.name,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.weight(1f),
    )
    Icon(
      imageVector = DayKeeperIcons.ChevronRight,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun DueDateRow(task: Task, modifier: Modifier = Modifier) {
  val taskDueDate = task.dueDate
  val taskDueAt = task.dueAt
  val dateText =
    when {
      taskDueDate != null -> taskDueDate
      taskDueAt != null -> formatEpochMillis(taskDueAt)
      else -> null
    } ?: return
  MetaRow(
    icon = {
      Icon(
        imageVector = DayKeeperIcons.Event,
        contentDescription = "Due date",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
    label = dateText,
    modifier = modifier,
  )
}

@Composable
private fun RecurrenceRow(rule: RecurrenceRule, modifier: Modifier = Modifier) {
  MetaRow(
    icon = {
      Icon(
        imageVector = DayKeeperIcons.Repeat,
        contentDescription = "Recurrence",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
    label = recurrenceSummary(rule),
    modifier = modifier,
  )
}

@Composable
private fun ReminderRow(minutesBefore: Int, modifier: Modifier = Modifier) {
  MetaRow(
    icon = {
      Icon(
        imageVector = DayKeeperIcons.Notification,
        contentDescription = "Reminder",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
    label = formatReminderMinutes(minutesBefore),
    modifier = modifier,
  )
}

@Composable
private fun MetaRow(icon: @Composable () -> Unit, label: String, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth().padding(vertical = MetaRowSpacing),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(MetaIconTextSpacing),
  ) {
    icon()
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun DescriptionSection(description: String, modifier: Modifier = Modifier) {
  Text(
    text = description,
    style = MaterialTheme.typography.bodyLarge,
    color = MaterialTheme.colorScheme.onSurface,
    modifier = modifier.fillMaxWidth(),
  )
}
