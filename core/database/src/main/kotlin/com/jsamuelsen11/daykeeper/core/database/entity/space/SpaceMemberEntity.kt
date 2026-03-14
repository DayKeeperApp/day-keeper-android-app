package com.jsamuelsen11.daykeeper.core.database.entity.space

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity

@Entity(
  tableName = "space_members",
  primaryKeys = ["space_id", "tenant_id"],
  foreignKeys =
    [
      ForeignKey(
        entity = SpaceEntity::class,
        parentColumns = ["space_id"],
        childColumns = ["space_id"],
        onDelete = ForeignKey.CASCADE,
      ),
      ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["tenant_id"],
        childColumns = ["tenant_id"],
        onDelete = ForeignKey.CASCADE,
      ),
    ],
  indices = [Index(value = ["tenant_id"])],
)
public data class SpaceMemberEntity(
  @ColumnInfo(name = "space_id") val spaceId: String,
  @ColumnInfo(name = "tenant_id") val tenantId: String,
  @ColumnInfo(name = "role") val role: String,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
