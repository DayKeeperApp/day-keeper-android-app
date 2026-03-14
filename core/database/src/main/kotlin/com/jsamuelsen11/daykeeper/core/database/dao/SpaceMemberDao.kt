package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface SpaceMemberDao {
  @Query("SELECT * FROM space_members WHERE space_id = :spaceId AND deleted_at IS NULL")
  public fun observeBySpace(spaceId: String): Flow<List<SpaceMemberEntity>>

  @Query("SELECT * FROM space_members WHERE space_id = :spaceId AND tenant_id = :tenantId")
  public suspend fun getByCompositeKey(spaceId: String, tenantId: String): SpaceMemberEntity?

  @Upsert public suspend fun upsert(entity: SpaceMemberEntity)

  @Upsert public suspend fun upsertAll(entities: List<SpaceMemberEntity>)

  @Query(
    "UPDATE space_members SET deleted_at = :deletedAt, updated_at = :updatedAt" +
      " WHERE space_id = :spaceId AND tenant_id = :tenantId"
  )
  public suspend fun softDelete(spaceId: String, tenantId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM space_members WHERE space_id = :spaceId AND tenant_id = :tenantId")
  public suspend fun hardDelete(spaceId: String, tenantId: String)
}
