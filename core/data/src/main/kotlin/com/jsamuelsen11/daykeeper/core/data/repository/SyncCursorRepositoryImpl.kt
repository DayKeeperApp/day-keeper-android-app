package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.SyncCursorDao
import com.jsamuelsen11.daykeeper.core.model.sync.SyncCursor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class SyncCursorRepositoryImpl(private val dao: SyncCursorDao) : SyncCursorRepository {
  public override fun observeCursor(): Flow<SyncCursor?> =
    dao.observeCursor().map { it?.toDomain() }

  public override suspend fun getCursor(): SyncCursor? = dao.getCursor()?.toDomain()

  public override suspend fun upsert(cursor: SyncCursor) {
    dao.upsert(cursor.toEntity())
  }
}
