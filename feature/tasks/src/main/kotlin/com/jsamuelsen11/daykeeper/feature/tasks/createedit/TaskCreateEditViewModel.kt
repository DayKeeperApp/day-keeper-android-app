package com.jsamuelsen11.daykeeper.feature.tasks.createedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.attachment.AttachmentManager
import com.jsamuelsen11.daykeeper.core.data.repository.AttachmentRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachableEntityType
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the task create/edit screen.
 *
 * Loads an existing task when [SavedStateHandle] contains a `taskId`, and pre-selects a project
 * when `projectId` is provided. Projects and categories are loaded reactively via [Flow] and kept
 * in sync for the lifetime of the ViewModel. When editing, attachments are observed and included in
 * the state.
 */
class TaskCreateEditViewModel(
  savedStateHandle: SavedStateHandle,
  private val taskRepository: TaskRepository,
  private val projectRepository: ProjectRepository,
  private val taskCategoryRepository: TaskCategoryRepository,
  private val attachmentRepository: AttachmentRepository,
  private val attachmentManager: AttachmentManager,
) : ViewModel() {

  private val taskId: String? = savedStateHandle[KEY_TASK_ID]
  private val initialProjectId: String? = savedStateHandle[KEY_PROJECT_ID]
  private val isEditing = taskId != null

  private val _uiState = MutableStateFlow<TaskCreateEditUiState>(TaskCreateEditUiState.Loading)

  /** The current UI state. Consumers should use [collectAsStateWithLifecycle]. */
  val uiState: StateFlow<TaskCreateEditUiState> = _uiState.asStateFlow()

  private val _events = Channel<TaskCreateEditEvent>(Channel.BUFFERED)

  /** One-shot events. Collect with [LaunchedEffect] in the composable. */
  val events = _events.receiveAsFlow()

  init {
    viewModelScope.launch {
      val existingTask = if (isEditing) taskRepository.getById(taskId!!) else null

      val attachmentsFlow =
        if (isEditing && taskId != null) {
          attachmentRepository.observeByEntity(AttachableEntityType.TASK, taskId).map { attachments
            ->
            attachments.map { attachment ->
              AttachmentUiItem(
                attachmentId = attachment.attachmentId,
                fileName = attachment.fileName,
                mimeType = attachment.mimeType,
                fileSize = attachment.fileSize,
                downloadState =
                  attachmentManager.observeDownloadState(attachment.attachmentId).value,
                remoteUrl = attachment.remoteUrl,
                localPath = attachment.localPath,
              )
            }
          }
        } else {
          flowOf(emptyList())
        }

      combine(
          projectRepository.observeBySpace(DEFAULT_SPACE_ID),
          taskCategoryRepository.observeAll(),
          attachmentsFlow,
        ) { projects, categories, attachments ->
          val current = _uiState.value
          when {
            current is TaskCreateEditUiState.Loading -> {
              if (existingTask != null) {
                TaskCreateEditUiState.Ready(
                  title = existingTask.title,
                  description = existingTask.description.orEmpty(),
                  projectId = existingTask.projectId,
                  priority = existingTask.priority,
                  categoryId = existingTask.categoryId,
                  dueDate = existingTask.dueDate,
                  dueAt = existingTask.dueAt,
                  recurrenceRule = existingTask.recurrenceRule,
                  reminderMinutesBefore = existingTask.reminderMinutesBefore,
                  isEditing = true,
                  projects = projects,
                  categories = categories,
                  attachments = attachments,
                )
              } else {
                TaskCreateEditUiState.Ready(
                  projectId = initialProjectId,
                  projects = projects,
                  categories = categories,
                )
              }
            }
            current is TaskCreateEditUiState.Ready ->
              current.copy(projects = projects, categories = categories, attachments = attachments)
            else -> current
          }
        }
        .collect { newState -> _uiState.value = newState }
    }
  }

  /** Updates the title and clears any existing title validation error. */
  fun onTitleChanged(title: String) {
    _uiState.update { state ->
      if (state is TaskCreateEditUiState.Ready) {
        state.copy(title = title, titleError = null)
      } else {
        state
      }
    }
  }

  /** Updates the description. */
  fun onDescriptionChanged(description: String) {
    _uiState.update { state ->
      if (state is TaskCreateEditUiState.Ready) state.copy(description = description) else state
    }
  }

  /** Selects the given project, or clears it when [projectId] is null. */
  fun onProjectSelected(projectId: String?) {
    _uiState.update { state ->
      if (state is TaskCreateEditUiState.Ready) state.copy(projectId = projectId) else state
    }
  }

  /** Updates the task priority. */
  fun onPrioritySelected(priority: Priority) {
    _uiState.update { state ->
      if (state is TaskCreateEditUiState.Ready) state.copy(priority = priority) else state
    }
  }

  /** Selects a category by ID, or clears it when [categoryId] is null. */
  fun onCategorySelected(categoryId: String?) {
    _uiState.update { state ->
      if (state is TaskCreateEditUiState.Ready) state.copy(categoryId = categoryId) else state
    }
  }

  /**
   * Stores the selected due date as an ISO-8601 string and clears the time component so that an
   * existing [dueAt] is not left stale after a date change.
   *
   * @param dateString ISO-8601 date string, e.g. "2026-03-16".
   */
  fun onDueDateSelected(dateString: String) {
    _uiState.update { state ->
      if (state is TaskCreateEditUiState.Ready) {
        state.copy(dueDate = dateString, dueAt = null)
      } else {
        state
      }
    }
  }

  /**
   * Stores the due time as epoch millis. Should only be called after a due date has been set via
   * [onDueDateSelected].
   *
   * @param epochMillis Epoch millis representing the full date + time instant.
   */
  fun onDueTimeSelected(epochMillis: Long) {
    _uiState.update { state ->
      if (state is TaskCreateEditUiState.Ready) state.copy(dueAt = epochMillis) else state
    }
  }

  /** Updates the recurrence rule. Pass null to remove recurrence. */
  fun onRecurrenceChanged(rule: RecurrenceRule?) {
    _uiState.update { state ->
      if (state is TaskCreateEditUiState.Ready) state.copy(recurrenceRule = rule) else state
    }
  }

  /** Updates the reminder offset. Pass null to remove the reminder. */
  fun onReminderChanged(minutesBefore: Int?) {
    _uiState.update { state ->
      if (state is TaskCreateEditUiState.Ready) {
        state.copy(reminderMinutesBefore = minutesBefore)
      } else {
        state
      }
    }
  }

  /**
   * Validates the form and persists the task. Emits [TaskCreateEditEvent.Saved] on success, or sets
   * [TaskCreateEditUiState.Ready.titleError] on validation failure, or surfaces a repository error
   * as a [titleError] message on unexpected failure.
   */
  fun onSave() {
    val state = (_uiState.value as? TaskCreateEditUiState.Ready)?.takeIf { !it.isSaving } ?: return
    val trimmedTitle = state.title.trim()
    if (trimmedTitle.isBlank()) {
      _uiState.update { current ->
        if (current is TaskCreateEditUiState.Ready) current.copy(titleError = TITLE_EMPTY_ERROR)
        else current
      }
      return
    }

    _uiState.update { current ->
      if (current is TaskCreateEditUiState.Ready) current.copy(isSaving = true) else current
    }

    viewModelScope.launch {
      val task = buildTask(state, trimmedTitle) ?: return@launch
      runCatching { taskRepository.upsert(task) }
        .onSuccess { _events.send(TaskCreateEditEvent.Saved) }
        .onFailure { error -> setSaveError(error.message ?: SAVE_FAILED_ERROR) }
    }
  }

  private suspend fun buildTask(state: TaskCreateEditUiState.Ready, trimmedTitle: String): Task? {
    val now = System.currentTimeMillis()
    return if (isEditing) buildEditedTask(state, trimmedTitle, now)
    else buildNewTask(state, trimmedTitle, now)
  }

  private suspend fun buildEditedTask(
    state: TaskCreateEditUiState.Ready,
    trimmedTitle: String,
    now: Long,
  ): Task? {
    val existing = taskRepository.getById(taskId!!)
    return existing?.copy(
      title = trimmedTitle,
      description = state.description.trim().ifBlank { null },
      projectId = state.projectId,
      priority = state.priority,
      categoryId = state.categoryId,
      dueDate = state.dueDate,
      dueAt = state.dueAt,
      recurrenceRule = state.recurrenceRule,
      reminderMinutesBefore = state.reminderMinutesBefore,
      updatedAt = now,
    )
      ?: run {
        resetSaving()
        null
      }
  }

  private fun buildNewTask(
    state: TaskCreateEditUiState.Ready,
    trimmedTitle: String,
    now: Long,
  ): Task =
    Task(
      taskId = UUID.randomUUID().toString(),
      spaceId = DEFAULT_SPACE_ID,
      tenantId = DEFAULT_TENANT_ID,
      title = trimmedTitle,
      description = state.description.trim().ifBlank { null },
      status = TaskStatus.TODO,
      priority = state.priority,
      projectId = state.projectId,
      categoryId = state.categoryId,
      dueDate = state.dueDate,
      dueAt = state.dueAt,
      recurrenceRule = state.recurrenceRule,
      reminderMinutesBefore = state.reminderMinutesBefore,
      createdAt = now,
      updatedAt = now,
    )

  private fun setSaveError(message: String) {
    _uiState.update { current ->
      if (current is TaskCreateEditUiState.Ready) {
        current.copy(isSaving = false, titleError = message)
      } else {
        current
      }
    }
  }

  private fun resetSaving() {
    _uiState.update { current ->
      if (current is TaskCreateEditUiState.Ready) current.copy(isSaving = false) else current
    }
  }

  /**
   * Initiates a download for the given attachment if it is not already cached locally.
   *
   * @param item The attachment to download.
   */
  fun downloadAttachment(item: AttachmentUiItem) {
    viewModelScope.launch {
      val attachment = attachmentRepository.getById(item.attachmentId) ?: return@launch
      attachmentManager.download(attachment)
    }
  }

  /**
   * Removes the locally cached file for the given attachment and deletes its metadata from the
   * repository.
   *
   * @param attachmentId The ID of the attachment to delete.
   */
  fun deleteAttachment(attachmentId: String) {
    viewModelScope.launch {
      attachmentManager.deleteLocal(attachmentId)
      attachmentRepository.delete(attachmentId)
    }
  }

  companion object {
    internal const val KEY_TASK_ID = "taskId"
    internal const val KEY_PROJECT_ID = "projectId"
    internal const val DEFAULT_SPACE_ID = "default-space"
    internal const val DEFAULT_TENANT_ID = "default-tenant"
    internal const val TITLE_EMPTY_ERROR = "Title cannot be empty"
    internal const val SAVE_FAILED_ERROR = "Save failed"
    /** Timeout for [StateFlow.stateIn] sharing, kept here for future use if the flow is shared. */
    internal const val STOP_TIMEOUT_MILLIS = 5_000L
  }
}
