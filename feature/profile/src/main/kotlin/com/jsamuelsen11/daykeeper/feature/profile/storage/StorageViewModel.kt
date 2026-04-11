package com.jsamuelsen11.daykeeper.feature.profile.storage

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val BYTES_PER_MB = 1_048_576L
private const val CACHE_DIR_NAME = "attachments"

class StorageViewModel(
  private val application: Application,
  private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

  private val cacheSizeRefresh = MutableStateFlow(0)

  val uiState: StateFlow<StorageUiState> =
    combine(userPreferencesRepository.userPreferences, cacheSizeRefresh) { prefs, _ ->
        StorageUiState.Success(
          currentCacheSizeMb = calculateCacheSizeMb(),
          maxCacheSizeMb = prefs.attachmentCacheSizeMb,
        )
      }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        StorageUiState.Loading,
      )

  fun updateMaxCacheSize(sizeMb: Int) {
    viewModelScope.launch { userPreferencesRepository.setAttachmentCacheSizeMb(sizeMb) }
  }

  fun clearCache() {
    viewModelScope.launch {
      val cacheDir = application.cacheDir.resolve(CACHE_DIR_NAME)
      if (cacheDir.exists()) {
        cacheDir.deleteRecursively()
      }
      cacheSizeRefresh.value++
    }
  }

  private fun calculateCacheSizeMb(): Long {
    val cacheDir = application.cacheDir.resolve(CACHE_DIR_NAME)
    if (!cacheDir.exists()) return 0L
    return cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() } / BYTES_PER_MB
  }
}
