package com.jsamuelsen11.daykeeper.feature.tasks.createedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.Project
import com.jsamuelsen11.daykeeper.core.ui.component.CategoryChip
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperDatePicker
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTimePicker
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.component.RecurrencePicker
import com.jsamuelsen11.daykeeper.core.ui.component.ReminderConfigurator
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import org.koin.compose.viewmodel.koinViewModel

// region Constants

private val ContentPadding = 16.dp
private val SectionSpacing = 20.dp
private val ItemSpacing = 8.dp
private val ChipSpacing = 8.dp
private val RowIconSpacing = 12.dp
private const val MIN_DESCRIPTION_LINES = 3
private const val LABEL_PROJECT_NONE = "No project"
private const val LABEL_MANAGE_CATEGORIES = "Manage"
private const val LABEL_SET_DUE_DATE = "Set due date"
private const val LABEL_SET_DUE_TIME = "Set time"
private const val LABEL_SET_RECURRENCE = "Set recurrence"
private const val LABEL_SET_REMINDER = "Set reminder"
private const val LABEL_PRIORITY_SECTION = "Priority"
private const val LABEL_CATEGORY_SECTION = "Category"
private const val LABEL_SAVE = "Save"

// endregion

/**
 * Screen for creating a new task or editing an existing one.
 *
 * @param onNavigateBack Called when navigation back is requested (back button or after save).
 * @param onManageCategories Called when the user taps "Manage" in the category section.
 * @param modifier Modifier applied to the [Scaffold].
 * @param viewModel Injected via Koin by default.
 */
@Composable
fun TaskCreateEditScreen(
  onNavigateBack: () -> Unit,
  onManageCategories: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: TaskCreateEditViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.events.collect { event -> if (event is TaskCreateEditEvent.Saved) onNavigateBack() }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      val title =
        when (val state = uiState) {
          is TaskCreateEditUiState.Ready -> if (state.isEditing) "Edit Task" else "New Task"
          else -> "Task"
        }
      DayKeeperTopAppBar(
        title = title,
        onNavigationClick = onNavigateBack,
        actions = {
          IconButton(
            onClick = viewModel::onSave,
            enabled =
              uiState !is TaskCreateEditUiState.Loading &&
                (uiState as? TaskCreateEditUiState.Ready)?.isSaving != true,
          ) {
            Icon(imageVector = DayKeeperIcons.Check, contentDescription = LABEL_SAVE)
          }
        },
      )
    },
  ) { innerPadding ->
    when (val state = uiState) {
      is TaskCreateEditUiState.Loading ->
        LoadingIndicator(modifier = Modifier.padding(innerPadding))
      is TaskCreateEditUiState.Ready ->
        TaskCreateEditContent(
          state = state,
          callbacks =
            TaskCreateEditCallbacks(
              onTitleChanged = viewModel::onTitleChanged,
              onDescriptionChanged = viewModel::onDescriptionChanged,
              onProjectSelected = viewModel::onProjectSelected,
              onPrioritySelected = viewModel::onPrioritySelected,
              onCategorySelected = viewModel::onCategorySelected,
              onDueDateSelected = viewModel::onDueDateSelected,
              onDueTimeSelected = viewModel::onDueTimeSelected,
              onRecurrenceChanged = viewModel::onRecurrenceChanged,
              onReminderChanged = viewModel::onReminderChanged,
              onManageCategories = onManageCategories,
            ),
          modifier = Modifier.padding(innerPadding),
        )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskCreateEditContent(
  state: TaskCreateEditUiState.Ready,
  callbacks: TaskCreateEditCallbacks,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }
  var showRecurrencePicker by remember { mutableStateOf(false) }
  var showReminderConfigurator by remember { mutableStateOf(false) }

  if (showDatePicker) {
    DayKeeperDatePicker(
      onDateSelected = { epochMillis ->
        callbacks.onDueDateSelected(formatEpochToDate(epochMillis))
        showDatePicker = false
      },
      onDismiss = { showDatePicker = false },
    )
  }
  if (showTimePicker) {
    DayKeeperTimePicker(
      onTimeSelected = { hour, minute ->
        val combinedMillis = buildDueAtMillis(state.dueDate, hour, minute)
        if (combinedMillis != null) callbacks.onDueTimeSelected(combinedMillis)
        showTimePicker = false
      },
      onDismiss = { showTimePicker = false },
    )
  }
  if (showRecurrencePicker) {
    RecurrencePicker(
      onRecurrenceSelected = { rule ->
        callbacks.onRecurrenceChanged(rule)
        showRecurrencePicker = false
      },
      onDismiss = { showRecurrencePicker = false },
      initialRule = state.recurrenceRule,
    )
  }
  if (showReminderConfigurator) {
    ReminderConfigurator(
      onReminderSelected = { minutes ->
        callbacks.onReminderChanged(minutes)
        showReminderConfigurator = false
      },
      onDismiss = { showReminderConfigurator = false },
      initialMinutesBefore = state.reminderMinutesBefore,
    )
  }

  Column(
    modifier = modifier.verticalScroll(rememberScrollState()).padding(ContentPadding),
    verticalArrangement = Arrangement.spacedBy(SectionSpacing),
  ) {
    TaskCreateEditFields(state = state, callbacks = callbacks)

    TaskCreateEditScheduleSection(
      state = state,
      onPickDate = { showDatePicker = true },
      onPickTime = { showTimePicker = true },
      onPickRecurrence = { showRecurrencePicker = true },
      onPickReminder = { showReminderConfigurator = true },
    )

    Spacer(modifier = Modifier.height(SectionSpacing))
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskCreateEditFields(
  state: TaskCreateEditUiState.Ready,
  callbacks: TaskCreateEditCallbacks,
) {
  // Title
  OutlinedTextField(
    value = state.title,
    onValueChange = callbacks.onTitleChanged,
    modifier = Modifier.fillMaxWidth(),
    label = { Text("Title") },
    isError = state.titleError != null,
    supportingText = state.titleError?.let { error -> { Text(error) } },
    singleLine = true,
    enabled = !state.isSaving,
  )

  // Description
  OutlinedTextField(
    value = state.description,
    onValueChange = callbacks.onDescriptionChanged,
    modifier = Modifier.fillMaxWidth(),
    label = { Text("Description") },
    minLines = MIN_DESCRIPTION_LINES,
    enabled = !state.isSaving,
  )

  // Project selector
  ProjectSelector(
    projects = state.projects,
    selectedProjectId = state.projectId,
    onProjectSelected = callbacks.onProjectSelected,
    enabled = !state.isSaving,
  )

  // Priority
  Column(verticalArrangement = Arrangement.spacedBy(ItemSpacing)) {
    Text(
      text = LABEL_PRIORITY_SECTION,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    PrioritySelector(
      selected = state.priority,
      onPrioritySelected = callbacks.onPrioritySelected,
      enabled = !state.isSaving,
    )
  }

  // Categories
  CategorySection(state = state, callbacks = callbacks)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(
  state: TaskCreateEditUiState.Ready,
  callbacks: TaskCreateEditCallbacks,
) {
  Column(verticalArrangement = Arrangement.spacedBy(ItemSpacing)) {
    Text(
      text = LABEL_CATEGORY_SECTION,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(ChipSpacing)) {
      state.categories.forEach { category ->
        CategoryChip(
          name = category.name,
          selected = category.categoryId == state.categoryId,
          onClick = {
            val newId = if (category.categoryId == state.categoryId) null else category.categoryId
            callbacks.onCategorySelected(newId)
          },
        )
      }
      FilterChip(
        selected = false,
        onClick = callbacks.onManageCategories,
        label = { Text(LABEL_MANAGE_CATEGORIES) },
      )
    }
  }
}

@Composable
private fun TaskCreateEditScheduleSection(
  state: TaskCreateEditUiState.Ready,
  onPickDate: () -> Unit,
  onPickTime: () -> Unit,
  onPickRecurrence: () -> Unit,
  onPickReminder: () -> Unit,
) {
  // Due date
  DueDateRow(dueDate = state.dueDate, onPickDate = onPickDate, enabled = !state.isSaving)

  // Due time (only shown once a date is selected)
  if (state.dueDate != null) {
    DueTimeRow(dueAt = state.dueAt, onPickTime = onPickTime, enabled = !state.isSaving)
  }

  // Recurrence
  RecurrenceRow(
    recurrenceRule = state.recurrenceRule,
    onPickRecurrence = onPickRecurrence,
    enabled = !state.isSaving,
  )

  // Reminder
  ReminderRow(
    reminderMinutesBefore = state.reminderMinutesBefore,
    onPickReminder = onPickReminder,
    enabled = !state.isSaving,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectSelector(
  projects: List<Project>,
  selectedProjectId: String?,
  onProjectSelected: (String?) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedName =
    projects.firstOrNull { it.projectId == selectedProjectId }?.name ?: LABEL_PROJECT_NONE

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { if (enabled) expanded = it },
    modifier = modifier,
  ) {
    OutlinedTextField(
      value = selectedName,
      onValueChange = {},
      modifier =
        Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
      label = { Text("Project") },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      readOnly = true,
      enabled = enabled,
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      DropdownMenuItem(
        text = { Text(LABEL_PROJECT_NONE) },
        onClick = {
          onProjectSelected(null)
          expanded = false
        },
      )
      projects.forEach { project ->
        DropdownMenuItem(
          text = { Text(project.name) },
          onClick = {
            onProjectSelected(project.projectId)
            expanded = false
          },
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrioritySelector(
  selected: Priority,
  onPrioritySelected: (Priority) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
    Priority.entries.forEachIndexed { index, priority ->
      SegmentedButton(
        selected = selected == priority,
        onClick = { onPrioritySelected(priority) },
        shape = SegmentedButtonDefaults.itemShape(index = index, count = Priority.entries.size),
        enabled = enabled,
        label = { Text(priorityLabel(priority)) },
      )
    }
  }
}

@Composable
private fun DueDateRow(
  dueDate: String?,
  onPickDate: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RowIconSpacing),
  ) {
    Icon(
      imageVector = DayKeeperIcons.Event,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    TextButton(onClick = onPickDate, enabled = enabled) { Text(dueDate ?: LABEL_SET_DUE_DATE) }
  }
}

@Composable
private fun DueTimeRow(
  dueAt: Long?,
  onPickTime: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RowIconSpacing),
  ) {
    Icon(
      imageVector = DayKeeperIcons.Schedule,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    TextButton(onClick = onPickTime, enabled = enabled) {
      Text(dueAt?.let { formatEpochToTime(it) } ?: LABEL_SET_DUE_TIME)
    }
  }
}

@Composable
private fun RecurrenceRow(
  recurrenceRule: com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule?,
  onPickRecurrence: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RowIconSpacing),
  ) {
    Icon(
      imageVector = DayKeeperIcons.Repeat,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    TextButton(onClick = onPickRecurrence, enabled = enabled) {
      Text(recurrenceRule?.toRruleString() ?: LABEL_SET_RECURRENCE)
    }
  }
}

@Composable
private fun ReminderRow(
  reminderMinutesBefore: Int?,
  onPickReminder: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RowIconSpacing),
  ) {
    Icon(
      imageVector = DayKeeperIcons.Notification,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    TextButton(onClick = onPickReminder, enabled = enabled) {
      Text(reminderMinutesBefore?.let { formatReminderLabel(it) } ?: LABEL_SET_REMINDER)
    }
  }
}
