package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.space.Space
import kotlinx.coroutines.flow.Flow

public interface SpaceRepository {
  public fun observeById(spaceId: String): Flow<Space?>

  public fun observeByTenant(tenantId: String): Flow<List<Space>>

  public suspend fun getById(spaceId: String): Space?

  public suspend fun upsert(space: Space)

  public suspend fun delete(spaceId: String)
}
