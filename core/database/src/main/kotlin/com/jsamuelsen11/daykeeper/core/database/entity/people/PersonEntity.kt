package com.jsamuelsen11.daykeeper.core.database.entity.people

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceEntity

@Entity(
  tableName = "persons",
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
  indices = [Index(value = ["space_id", "updated_at"]), Index(value = ["tenant_id"])],
)
public data class PersonEntity(
  @PrimaryKey @ColumnInfo(name = "person_id") val personId: String,
  @ColumnInfo(name = "space_id") val spaceId: String,
  @ColumnInfo(name = "tenant_id") val tenantId: String,
  @ColumnInfo(name = "first_name") val firstName: String,
  @ColumnInfo(name = "last_name") val lastName: String,
  @ColumnInfo(name = "nickname") val nickname: String?,
  @ColumnInfo(name = "notes") val notes: String?,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
