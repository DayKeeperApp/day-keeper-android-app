package com.jsamuelsen11.daykeeper.feature.profile.sync

sealed interface SyncStatusUiState {
  data object Loading : SyncStatusUiState

  data class Success(
    val status: String,
    val isSyncing: Boolean,
    val lastSyncFormatted: String,
  ) : SyncStatusUiState
}
