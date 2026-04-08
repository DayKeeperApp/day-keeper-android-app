package com.jsamuelsen11.daykeeper.feature.profile.overview

sealed interface ProfileOverviewUiState {
  data object Loading : ProfileOverviewUiState

  data class Success(
    val displayName: String,
    val email: String,
  ) : ProfileOverviewUiState

  data class Error(val message: String) : ProfileOverviewUiState
}
