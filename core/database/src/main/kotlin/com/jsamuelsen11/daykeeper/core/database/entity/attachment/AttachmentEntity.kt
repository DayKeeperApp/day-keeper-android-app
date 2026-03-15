package com.jsamuelsen11.daykeeper.core.database.entity.attachment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceEntity

@Entity(
  tableName = "attachments",
  foreignKeys =
    [
      ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["tenant_id"],
        childColumns = ["tenant_id"],
        onDelete = ForeignKey.CASCADE,
      ),
      ForeignKey(
        entity = SpaceEntity::class,
        parentColumns = ["space_id"],
        childColumns = ["space_id"],
        onDelete = ForeignKey.CASCADE,
      ),
    ],
  indices =
    [
      Index(value = ["entity_type", "entity_id"]),
      Index(value = ["space_id", "updated_at"]),
      Index(value = ["tenant_id"]),
    ],
)
public data class AttachmentEntity(
  @PrimaryKey @ColumnInfo(name = "attachment_id") val attachmentId: String,
  @ColumnInfo(name = "entity_type") val entityType: String,
  @ColumnInfo(name = "entity_id") val entityId: String,
  @ColumnInfo(name = "tenant_id") val tenantId: String,
  @ColumnInfo(name = "space_id") val spaceId: String,
  @ColumnInfo(name = "file_name") val fileName: String,
  @ColumnInfo(name = "mime_type") val mimeType: String,
  @ColumnInfo(name = "file_size") val fileSize: Long,
  @ColumnInfo(name = "remote_url") val remoteUrl: String?,
  @ColumnInfo(name = "local_path") val localPath: String?,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
