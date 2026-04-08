package com.jsamuelsen11.daykeeper.feature.profile.storage

sealed interface StorageUiState {
  data object Loading : StorageUiState

  data class Success(
    val currentCacheSizeMb: Long,
    val maxCacheSizeMb: Int,
  ) : StorageUiState
}
