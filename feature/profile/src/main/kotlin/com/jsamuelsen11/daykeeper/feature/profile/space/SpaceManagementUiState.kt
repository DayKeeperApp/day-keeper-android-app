package com.jsamuelsen11.daykeeper.feature.profile.space

import com.jsamuelsen11.daykeeper.core.model.space.Space
import com.jsamuelsen11.daykeeper.core.model.space.SpaceRole
import com.jsamuelsen11.daykeeper.core.model.space.SpaceType

sealed interface SpaceManagementUiState {
  data object Loading : SpaceManagementUiState

  data class Success(val groupedSpaces: Map<SpaceType, List<SpaceWithMeta>>) :
    SpaceManagementUiState

  data class Error(val message: String) : SpaceManagementUiState
}

data class SpaceWithMeta(val space: Space, val memberCount: Int, val userRole: SpaceRole?)
