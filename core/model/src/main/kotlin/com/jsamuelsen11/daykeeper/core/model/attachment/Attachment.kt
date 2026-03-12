package com.jsamuelsen11.daykeeper.core.model.attachment

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A file attached to an event, task, or person. */
data class Attachment(
  val attachmentId: String,
  val entityType: AttachableEntityType,
  val entityId: String,
  val tenantId: String,
  val spaceId: String,
  val fileName: String,
  val mimeType: String,
  val fileSize: Long,
  val remoteUrl: String? = null,
  val localPath: String? = null,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
