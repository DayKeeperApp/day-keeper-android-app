package com.jsamuelsen11.daykeeper.core.database.entity.space

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity

@Entity(
  tableName = "spaces",
  foreignKeys =
    [
      ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["tenant_id"],
        childColumns = ["tenant_id"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index(value = ["tenant_id"]), Index(value = ["space_id", "updated_at"])],
)
public data class SpaceEntity(
  @PrimaryKey @ColumnInfo(name = "space_id") val spaceId: String,
  @ColumnInfo(name = "tenant_id") val tenantId: String,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "normalized_name") val normalizedName: String,
  @ColumnInfo(name = "type") val type: String,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
