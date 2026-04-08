package com.jsamuelsen11.daykeeper.feature.profile.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.SyncCursorRepository
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatus
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatusProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val SYNC_TIME_FORMAT = "MMM d, yyyy h:mm a"

class SyncStatusViewModel(
  private val syncStatusProvider: SyncStatusProvider,
  syncCursorRepository: SyncCursorRepository,
) : ViewModel() {

  val uiState: StateFlow<SyncStatusUiState> =
    combine(syncStatusProvider.syncStatus, syncCursorRepository.observeCursor()) {
        syncStatus,
        cursor ->
        val statusLabel =
          when (syncStatus) {
            is SyncStatus.Idle -> "Idle"
            is SyncStatus.Syncing -> "Syncing..."
            is SyncStatus.Error -> "Error: ${syncStatus.message}"
          }
        SyncStatusUiState.Success(
          status = statusLabel,
          isSyncing = syncStatus is SyncStatus.Syncing,
          lastSyncFormatted = formatSyncTime(cursor?.lastSyncAt),
        )
      }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        SyncStatusUiState.Loading,
      )

  fun syncNow() {
    syncStatusProvider.requestSync()
  }

  private fun formatSyncTime(timestamp: Long?): String =
    if (timestamp == null || timestamp == 0L) "Never"
    else SimpleDateFormat(SYNC_TIME_FORMAT, Locale.getDefault()).format(Date(timestamp))
}
