package com.jsamuelsen11.daykeeper.feature.lists.createedit

sealed interface ListCreateEditUiState {
  data object Loading : ListCreateEditUiState

  data class Ready(
    val name: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null,
  ) : ListCreateEditUiState
}
