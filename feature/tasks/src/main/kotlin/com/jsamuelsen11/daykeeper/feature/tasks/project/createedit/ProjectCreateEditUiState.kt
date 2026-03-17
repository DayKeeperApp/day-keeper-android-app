package com.jsamuelsen11.daykeeper.feature.tasks.project.createedit

/** Represents the possible UI states for the project create/edit screen. */
public sealed interface ProjectCreateEditUiState {
  /** Initial load is in progress (e.g., fetching existing project data for edit mode). */
  public data object Loading : ProjectCreateEditUiState

  /**
   * The form is ready for user interaction.
   *
   * @property name Current value of the project name field.
   * @property description Current value of the optional description field.
   * @property isEditing `true` when editing an existing project, `false` when creating a new one.
   * @property isSaving `true` while the save operation is in flight.
   * @property nameError Non-null validation message when the name field is invalid.
   */
  public data class Ready(
    val name: String = "",
    val description: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null,
  ) : ProjectCreateEditUiState
}

/** One-shot events emitted by [ProjectCreateEditViewModel] to the UI. */
public sealed interface ProjectCreateEditEvent {
  /** Emitted when a save operation completes successfully. */
  public data object Saved : ProjectCreateEditEvent
}
