package com.jsamuelsen11.daykeeper.core.model.sync

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** Tracks the last sync position with the server. */
data class SyncCursor(val lastCursor: Long, val lastSyncAt: Long) : DayKeeperModel
