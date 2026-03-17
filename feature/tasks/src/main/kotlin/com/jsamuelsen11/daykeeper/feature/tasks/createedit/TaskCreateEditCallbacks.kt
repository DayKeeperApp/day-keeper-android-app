package com.jsamuelsen11.daykeeper.feature.tasks.createedit

import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.model.task.Priority

/** Groups all callback lambdas passed to [TaskCreateEditContent] to reduce parameter count. */
internal data class TaskCreateEditCallbacks(
  val onTitleChanged: (String) -> Unit,
  val onDescriptionChanged: (String) -> Unit,
  val onProjectSelected: (String?) -> Unit,
  val onPrioritySelected: (Priority) -> Unit,
  val onCategorySelected: (String?) -> Unit,
  val onDueDateSelected: (String) -> Unit,
  val onDueTimeSelected: (Long) -> Unit,
  val onRecurrenceChanged: (RecurrenceRule?) -> Unit,
  val onReminderChanged: (Int?) -> Unit,
  val onManageCategories: () -> Unit,
)
