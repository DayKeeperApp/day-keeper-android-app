package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.attachment.AttachableEntityType
import com.jsamuelsen11.daykeeper.core.model.attachment.Attachment
import kotlinx.coroutines.flow.Flow

public interface AttachmentRepository {
  public fun observeByEntity(
    entityType: AttachableEntityType,
    entityId: String,
  ): Flow<List<Attachment>>

  public suspend fun getById(attachmentId: String): Attachment?

  public suspend fun upsert(attachment: Attachment)

  public suspend fun delete(attachmentId: String)
}
