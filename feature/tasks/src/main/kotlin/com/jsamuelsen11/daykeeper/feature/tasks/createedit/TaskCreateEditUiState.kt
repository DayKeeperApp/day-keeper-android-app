package com.jsamuelsen11.daykeeper.feature.tasks.createedit

import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.Project
import com.jsamuelsen11.daykeeper.core.model.task.TaskCategory

/** UI state for the task create/edit screen. */
sealed interface TaskCreateEditUiState {
  /** Initial state while loading an existing task or seeding defaults. */
  data object Loading : TaskCreateEditUiState

  /**
   * Form is ready for interaction.
   *
   * @property title Current value of the title field.
   * @property description Current value of the description field.
   * @property projectId ID of the selected project, or null for no project.
   * @property priority Selected priority level.
   * @property categoryId ID of the selected category, or null for none.
   * @property dueDate ISO-8601 date string (e.g. "2026-03-16"), or null if not set.
   * @property dueAt Epoch millis of the combined due date + time, or null if not set.
   * @property recurrenceRule Recurrence rule for the task, or null for one-time tasks.
   * @property reminderMinutesBefore Minutes before due time to remind, or null for no reminder.
   * @property isEditing Whether the screen is editing an existing task.
   * @property isSaving Whether a save operation is in progress.
   * @property titleError Validation error message for the title field, or null if valid.
   * @property projects Available projects to assign the task to.
   * @property categories Available categories to tag the task with.
   */
  data class Ready(
    val title: String = "",
    val description: String = "",
    val projectId: String? = null,
    val priority: Priority = Priority.NONE,
    val categoryId: String? = null,
    val dueDate: String? = null,
    val dueAt: Long? = null,
    val recurrenceRule: RecurrenceRule? = null,
    val reminderMinutesBefore: Int? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val projects: List<Project> = emptyList(),
    val categories: List<TaskCategory> = emptyList(),
  ) : TaskCreateEditUiState
}

/** One-shot events emitted by [TaskCreateEditViewModel]. */
sealed interface TaskCreateEditEvent {
  /** Emitted after the task has been successfully saved. */
  data object Saved : TaskCreateEditEvent
}
