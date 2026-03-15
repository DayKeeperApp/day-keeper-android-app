package com.jsamuelsen11.daykeeper.core.data.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.sync.SyncCursorEntity
import com.jsamuelsen11.daykeeper.core.model.sync.SyncCursor

private const val SYNC_CURSOR_ID = "sync_cursor"

public fun SyncCursorEntity.toDomain(): SyncCursor =
  SyncCursor(lastCursor = lastCursor, lastSyncAt = lastSyncAt)

public fun SyncCursor.toEntity(): SyncCursorEntity =
  SyncCursorEntity(id = SYNC_CURSOR_ID, lastCursor = lastCursor, lastSyncAt = lastSyncAt)
