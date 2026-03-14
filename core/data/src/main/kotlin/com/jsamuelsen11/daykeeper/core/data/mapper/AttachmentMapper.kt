package com.jsamuelsen11.daykeeper.core.data.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.attachment.AttachmentEntity
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachableEntityType
import com.jsamuelsen11.daykeeper.core.model.attachment.Attachment

public fun AttachmentEntity.toDomain(): Attachment =
  Attachment(
    attachmentId = attachmentId,
    entityType = AttachableEntityType.valueOf(entityType),
    entityId = entityId,
    tenantId = tenantId,
    spaceId = spaceId,
    fileName = fileName,
    mimeType = mimeType,
    fileSize = fileSize,
    remoteUrl = remoteUrl,
    localPath = localPath,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun Attachment.toEntity(): AttachmentEntity =
  AttachmentEntity(
    attachmentId = attachmentId,
    entityType = entityType.name,
    entityId = entityId,
    tenantId = tenantId,
    spaceId = spaceId,
    fileName = fileName,
    mimeType = mimeType,
    fileSize = fileSize,
    remoteUrl = remoteUrl,
    localPath = localPath,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )
