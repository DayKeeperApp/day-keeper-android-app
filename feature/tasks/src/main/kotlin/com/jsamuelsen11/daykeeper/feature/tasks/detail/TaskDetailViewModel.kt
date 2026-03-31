package com.jsamuelsen11.daykeeper.feature.tasks.detail

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
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

/**
 * ViewModel for the task detail screen.
 *
 * Observes a single task by ID (extracted from [SavedStateHandle]) and resolves its associated
 * [com.jsamuelsen11.daykeeper.core.model.task.Project] and
 * [com.jsamuelsen11.daykeeper.core.model.task.TaskCategory] reactively. Exposes [uiState] as a
 * [StateFlow] of [TaskDetailUiState].
 *
 * @param savedStateHandle Navigation back-stack handle; must contain a `taskId` key.
 * @param taskRepository Source of truth for task data.
 * @param projectRepository Source of truth for project data.
 * @param taskCategoryRepository Source of truth for task-category data.
 * @param attachmentRepository Source of truth for attachment metadata.
 * @param attachmentManager Manages local caching and downloading of attachments.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val taskRepository: TaskRepository,
  private val projectRepository: ProjectRepository,
  private val taskCategoryRepository: TaskCategoryRepository,
  private val attachmentRepository: AttachmentRepository,
  private val attachmentManager: AttachmentManager,
) : ViewModel() {

  private val taskId: String = checkNotNull(savedStateHandle["taskId"])

  /** The reactive UI state for this screen. Starts as [TaskDetailUiState.Loading]. */
  val uiState: StateFlow<TaskDetailUiState> =
    taskRepository
      .observeById(taskId)
      .flatMapLatest { task ->
        if (task == null) {
          flowOf(TaskDetailUiState.Error("Task not found"))
        } else {
          val taskProjectId = task.projectId
          val projectFlow =
            if (taskProjectId != null) {
              projectRepository.observeById(taskProjectId)
            } else {
              flowOf(null)
            }

          val categoryFlow =
            taskCategoryRepository.observeAll().map { categories ->
              categories.find { it.categoryId == task.categoryId }
            }

          val attachmentsFlow =
            attachmentRepository.observeByEntity(AttachableEntityType.TASK, taskId).flatMapLatest {
              attachments ->
              if (attachments.isEmpty()) {
                flowOf(emptyList())
              } else {
                combine(
                  attachments.map { attachment ->
                    attachmentManager.observeDownloadState(attachment.attachmentId).map {
                      downloadState ->
                      AttachmentUiItem(
                        attachmentId = attachment.attachmentId,
                        fileName = attachment.fileName,
                        mimeType = attachment.mimeType,
                        fileSize = attachment.fileSize,
                        downloadState = downloadState,
                        remoteUrl = attachment.remoteUrl,
                        localPath = attachment.localPath,
                      )
                    }
                  }
                ) { items ->
                  items.toList()
                }
              }
            }

          combine(projectFlow, categoryFlow, attachmentsFlow) { project, category, attachments ->
            TaskDetailUiState.Success(
              task = task,
              project = project,
              category = category,
              attachments = attachments,
            )
          }
        }
      }
      .catch { e -> emit(TaskDetailUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        TaskDetailUiState.Loading,
      )

  /**
   * Toggles the task completion status between [TaskStatus.TODO] and [TaskStatus.DONE]. When called
   * on a task that is [TaskStatus.DONE] the status reverts to [TaskStatus.TODO]; all other statuses
   * are promoted to [TaskStatus.DONE].
   */
  fun toggleComplete() {
    val state = uiState.value as? TaskDetailUiState.Success ?: return
    viewModelScope.launch {
      val task = state.task
      val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
      taskRepository.upsert(task.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
    }
  }

  /** Permanently deletes the current task from the repository. */
  fun deleteTask() {
    viewModelScope.launch { taskRepository.delete(taskId) }
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
}
