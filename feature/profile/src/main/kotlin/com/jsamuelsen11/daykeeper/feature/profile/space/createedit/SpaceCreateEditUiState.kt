package com.jsamuelsen11.daykeeper.feature.profile.space.createedit

import com.jsamuelsen11.daykeeper.core.model.space.SpaceMember
import com.jsamuelsen11.daykeeper.core.model.space.SpaceType

sealed interface SpaceCreateEditUiState {
  data object Loading : SpaceCreateEditUiState

  data class Success(
    val isEditMode: Boolean,
    val name: String,
    val type: SpaceType,
    val members: List<SpaceMember>,
    val isSaving: Boolean = false,
  ) : SpaceCreateEditUiState

  data class Error(val message: String) : SpaceCreateEditUiState
}
