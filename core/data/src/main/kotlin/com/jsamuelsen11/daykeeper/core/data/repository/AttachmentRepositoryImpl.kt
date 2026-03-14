package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.AttachmentDao
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachableEntityType
import com.jsamuelsen11.daykeeper.core.model.attachment.Attachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class AttachmentRepositoryImpl(private val dao: AttachmentDao) : AttachmentRepository {
  public override fun observeByEntity(
    entityType: AttachableEntityType,
    entityId: String,
  ): Flow<List<Attachment>> =
    dao.observeByEntity(entityType.name, entityId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(attachmentId: String): Attachment? =
    dao.getById(attachmentId)?.toDomain()

  public override suspend fun upsert(attachment: Attachment) {
    dao.upsert(attachment.toEntity())
  }

  public override suspend fun delete(attachmentId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(attachmentId, deletedAt = now, updatedAt = now)
  }
}
