package com.jsamuelsen11.daykeeper.core.database.entity.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "event_reminders",
  foreignKeys =
    [
      ForeignKey(
        entity = EventEntity::class,
        parentColumns = ["event_id"],
        childColumns = ["event_id"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index(value = ["event_id"])],
)
public data class EventReminderEntity(
  @PrimaryKey @ColumnInfo(name = "reminder_id") val reminderId: String,
  @ColumnInfo(name = "event_id") val eventId: String,
  @ColumnInfo(name = "minutes_before") val minutesBefore: Int,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
