package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.SpaceDao
import com.jsamuelsen11.daykeeper.core.model.space.Space
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class SpaceRepositoryImpl(private val dao: SpaceDao) : SpaceRepository {
  public override fun observeById(spaceId: String): Flow<Space?> =
    dao.observeById(spaceId).map { it?.toDomain() }

  public override fun observeByTenant(tenantId: String): Flow<List<Space>> =
    dao.observeByTenant(tenantId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(spaceId: String): Space? = dao.getById(spaceId)?.toDomain()

  public override suspend fun upsert(space: Space) {
    dao.upsert(space.toEntity())
  }

  public override suspend fun delete(spaceId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(spaceId, deletedAt = now, updatedAt = now)
  }
}
