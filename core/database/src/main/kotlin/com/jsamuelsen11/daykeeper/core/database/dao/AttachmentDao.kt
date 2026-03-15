package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.attachment.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface AttachmentDao {

  @Query(
    "SELECT * FROM attachments WHERE entity_type = :entityType AND entity_id = :entityId AND deleted_at IS NULL"
  )
  public fun observeByEntity(entityType: String, entityId: String): Flow<List<AttachmentEntity>>

  @Query("SELECT * FROM attachments WHERE attachment_id = :attachmentId")
  public suspend fun getById(attachmentId: String): AttachmentEntity?

  @Upsert public suspend fun upsert(entity: AttachmentEntity)

  @Upsert public suspend fun upsertAll(entities: List<AttachmentEntity>)

  @Query(
    "UPDATE attachments SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE attachment_id = :attachmentId"
  )
  public suspend fun softDelete(attachmentId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM attachments WHERE attachment_id = :attachmentId")
  public suspend fun hardDelete(attachmentId: String)
}
