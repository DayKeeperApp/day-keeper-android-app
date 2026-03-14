package com.jsamuelsen11.daykeeper.core.database.entity.sync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_cursors")
public data class SyncCursorEntity(
  @PrimaryKey @ColumnInfo(name = "id") val id: String,
  @ColumnInfo(name = "last_cursor") val lastCursor: Long,
  @ColumnInfo(name = "last_sync_at") val lastSyncAt: Long,
)
