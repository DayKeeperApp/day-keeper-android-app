package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.space.SpaceMember
import kotlinx.coroutines.flow.Flow

public interface SpaceMemberRepository {
  public fun observeBySpace(spaceId: String): Flow<List<SpaceMember>>

  public suspend fun getByCompositeKey(spaceId: String, tenantId: String): SpaceMember?

  public suspend fun upsert(member: SpaceMember)

  public suspend fun delete(spaceId: String, tenantId: String)
}
