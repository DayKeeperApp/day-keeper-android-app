package com.jsamuelsen11.daykeeper.core.database.entity.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceEntity

@Entity(
  tableName = "calendars",
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
public data class CalendarEntity(
  @PrimaryKey @ColumnInfo(name = "calendar_id") val calendarId: String,
  @ColumnInfo(name = "space_id") val spaceId: String,
  @ColumnInfo(name = "tenant_id") val tenantId: String,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "normalized_name") val normalizedName: String,
  @ColumnInfo(name = "color") val color: String,
  @ColumnInfo(name = "is_default") val isDefault: Boolean,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
