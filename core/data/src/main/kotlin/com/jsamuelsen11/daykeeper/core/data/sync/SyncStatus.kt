package com.jsamuelsen11.daykeeper.core.data.sync

/** Observable status of the sync engine. */
sealed interface SyncStatus {
  data object Idle : SyncStatus

  data object Syncing : SyncStatus

  data class Error(val message: String) : SyncStatus
}
