package com.jsamuelsen11.daykeeper.core.data.sync

import kotlinx.coroutines.flow.StateFlow

/** Provides observable sync status and the ability to trigger a manual sync. */
interface SyncStatusProvider {
  val syncStatus: StateFlow<SyncStatus>

  fun requestSync()
}
