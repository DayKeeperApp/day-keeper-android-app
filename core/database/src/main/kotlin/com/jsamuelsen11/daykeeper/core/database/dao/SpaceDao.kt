package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface SpaceDao {
  @Query("SELECT * FROM spaces WHERE space_id = :spaceId AND deleted_at IS NULL")
  public fun observeById(spaceId: String): Flow<SpaceEntity?>

  @Query("SELECT * FROM spaces WHERE tenant_id = :tenantId AND deleted_at IS NULL")
  public fun observeByTenant(tenantId: String): Flow<List<SpaceEntity>>

  @Query("SELECT * FROM spaces WHERE space_id = :spaceId")
  public suspend fun getById(spaceId: String): SpaceEntity?

  @Upsert public suspend fun upsert(entity: SpaceEntity)

  @Upsert public suspend fun upsertAll(entities: List<SpaceEntity>)

  @Query(
    "UPDATE spaces SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE space_id = :spaceId"
  )
  public suspend fun softDelete(spaceId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM spaces WHERE space_id = :spaceId")
  public suspend fun hardDelete(spaceId: String)
}
