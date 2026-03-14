package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.sync.SyncCursorEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface SyncCursorDao {

  @Query("SELECT * FROM sync_cursors LIMIT 1") public fun observeCursor(): Flow<SyncCursorEntity?>

  @Query("SELECT * FROM sync_cursors LIMIT 1") public suspend fun getCursor(): SyncCursorEntity?

  @Upsert public suspend fun upsert(entity: SyncCursorEntity)
}
