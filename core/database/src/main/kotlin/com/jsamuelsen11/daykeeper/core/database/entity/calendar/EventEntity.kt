package com.jsamuelsen11.daykeeper.core.database.entity.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceEntity

@Entity(
  tableName = "events",
  foreignKeys =
    [
      ForeignKey(
        entity = CalendarEntity::class,
        parentColumns = ["calendar_id"],
        childColumns = ["calendar_id"],
        onDelete = ForeignKey.CASCADE,
      ),
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
      ForeignKey(
        entity = EventTypeEntity::class,
        parentColumns = ["event_type_id"],
        childColumns = ["event_type_id"],
        onDelete = ForeignKey.SET_NULL,
      ),
    ],
  indices =
    [
      Index(value = ["calendar_id", "start_at"]),
      Index(value = ["calendar_id", "start_date"]),
      Index(value = ["space_id", "updated_at"]),
      Index(value = ["tenant_id"]),
      Index(value = ["event_type_id"]),
      Index(value = ["parent_event_id"]),
    ],
)
public data class EventEntity(
  @PrimaryKey @ColumnInfo(name = "event_id") val eventId: String,
  @ColumnInfo(name = "calendar_id") val calendarId: String,
  @ColumnInfo(name = "space_id") val spaceId: String,
  @ColumnInfo(name = "tenant_id") val tenantId: String,
  @ColumnInfo(name = "title") val title: String,
  @ColumnInfo(name = "description") val description: String?,
  @ColumnInfo(name = "start_at") val startAt: Long?,
  @ColumnInfo(name = "end_at") val endAt: Long?,
  @ColumnInfo(name = "start_date") val startDate: String?,
  @ColumnInfo(name = "end_date") val endDate: String?,
  @ColumnInfo(name = "is_all_day") val isAllDay: Boolean,
  @ColumnInfo(name = "timezone") val timezone: String,
  @ColumnInfo(name = "event_type_id") val eventTypeId: String?,
  @ColumnInfo(name = "location") val location: String?,
  @ColumnInfo(name = "recurrence_rule") val recurrenceRule: String?,
  @ColumnInfo(name = "parent_event_id") val parentEventId: String?,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
