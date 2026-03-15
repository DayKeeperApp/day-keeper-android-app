package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.sync.SyncCursor
import kotlinx.coroutines.flow.Flow

public interface SyncCursorRepository {
  public fun observeCursor(): Flow<SyncCursor?>

  public suspend fun getCursor(): SyncCursor?

  public suspend fun upsert(cursor: SyncCursor)
}
