package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.SpaceMemberDao
import com.jsamuelsen11.daykeeper.core.model.space.SpaceMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class SpaceMemberRepositoryImpl(private val dao: SpaceMemberDao) : SpaceMemberRepository {
  public override fun observeBySpace(spaceId: String): Flow<List<SpaceMember>> =
    dao.observeBySpace(spaceId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getByCompositeKey(spaceId: String, tenantId: String): SpaceMember? =
    dao.getByCompositeKey(spaceId, tenantId)?.toDomain()

  public override suspend fun upsert(member: SpaceMember) {
    dao.upsert(member.toEntity())
  }

  public override suspend fun delete(spaceId: String, tenantId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(spaceId, tenantId, deletedAt = now, updatedAt = now)
  }
}
