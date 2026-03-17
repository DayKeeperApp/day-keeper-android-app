package com.jsamuelsen11.daykeeper.feature.tasks.project.createedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.model.task.Project
import com.jsamuelsen11.daykeeper.core.model.task.ProjectStatus
import com.jsamuelsen11.daykeeper.feature.tasks.navigation.ProjectCreateEditRoute
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for creating or editing a [Project].
 *
 * Determines create-vs-edit mode from the optional `projectId` in [SavedStateHandle]. Emits
 * [ProjectCreateEditEvent.Saved] through [events] when a save succeeds so the screen can navigate
 * away.
 */
public class ProjectCreateEditViewModel(
  savedStateHandle: SavedStateHandle,
  private val projectRepository: ProjectRepository,
) : ViewModel() {

  private val projectId: String? = savedStateHandle.toRoute<ProjectCreateEditRoute>().projectId

  private val _uiState: MutableStateFlow<ProjectCreateEditUiState> =
    MutableStateFlow(ProjectCreateEditUiState.Loading)

  /** The current UI state. */
  public val uiState: StateFlow<ProjectCreateEditUiState> = _uiState.asStateFlow()

  private val _events: Channel<ProjectCreateEditEvent> = Channel(Channel.BUFFERED)

  /** One-shot events to be consumed by the screen. */
  public val events = _events.receiveAsFlow()

  init {
    if (projectId != null) {
      viewModelScope.launch {
        val existing = projectRepository.getById(projectId)
        if (existing != null) {
          _uiState.value =
            ProjectCreateEditUiState.Ready(
              name = existing.name,
              description = existing.description.orEmpty(),
              isEditing = true,
            )
        } else {
          _uiState.value = ProjectCreateEditUiState.Ready(isEditing = true)
        }
      }
    } else {
      _uiState.value = ProjectCreateEditUiState.Ready(isEditing = false)
    }
  }

  /**
   * Updates the project name field and clears any existing name validation error.
   *
   * @param name The new name string entered by the user.
   */
  public fun onNameChanged(name: String) {
    _uiState.update { current ->
      (current as? ProjectCreateEditUiState.Ready)?.copy(name = name, nameError = null) ?: current
    }
  }

  /**
   * Updates the description field.
   *
   * @param description The new description string entered by the user.
   */
  public fun onDescriptionChanged(description: String) {
    _uiState.update { current ->
      (current as? ProjectCreateEditUiState.Ready)?.copy(description = description) ?: current
    }
  }

  /**
   * Validates inputs and persists the project.
   *
   * Validates that [ProjectCreateEditUiState.Ready.name] is non-blank. On success the project is
   * upserted and [ProjectCreateEditEvent.Saved] is emitted.
   */
  public fun onSave() {
    val current = _uiState.value as? ProjectCreateEditUiState.Ready ?: return

    if (current.name.isBlank()) {
      _uiState.update {
        (it as? ProjectCreateEditUiState.Ready)?.copy(nameError = "Name is required") ?: it
      }
      return
    }

    _uiState.update { (it as? ProjectCreateEditUiState.Ready)?.copy(isSaving = true) ?: it }

    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val existing = projectId?.let { projectRepository.getById(it) }

      val project =
        existing?.copy(
          name = current.name.trim(),
          normalizedName = current.name.trim().lowercase(),
          description = current.description.trim().ifBlank { null },
          updatedAt = now,
        )
          ?: Project(
            projectId = UUID.randomUUID().toString(),
            spaceId = DEFAULT_SPACE_ID,
            tenantId = DEFAULT_TENANT_ID,
            name = current.name.trim(),
            normalizedName = current.name.trim().lowercase(),
            description = current.description.trim().ifBlank { null },
            status = ProjectStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
          )

      projectRepository.upsert(project)
      _uiState.update { (it as? ProjectCreateEditUiState.Ready)?.copy(isSaving = false) ?: it }
      _events.send(ProjectCreateEditEvent.Saved)
    }
  }

  public companion object {
    /** Timeout before the upstream flow is stopped after the last subscriber disappears. */
    public const val STOP_TIMEOUT_MILLIS: Long = 5_000L

    /** Fallback space ID used when no explicit space context is available. */
    public const val DEFAULT_SPACE_ID: String = "default-space"

    /** Fallback tenant ID used when no explicit tenant context is available. */
    public const val DEFAULT_TENANT_ID: String = "default-tenant"
  }
}
