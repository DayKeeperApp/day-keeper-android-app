package com.jsamuelsen11.daykeeper.core.database.entity.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_types")
public data class EventTypeEntity(
  @PrimaryKey @ColumnInfo(name = "event_type_id") val eventTypeId: String,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "normalized_name") val normalizedName: String,
  @ColumnInfo(name = "is_system") val isSystem: Boolean,
  @ColumnInfo(name = "color") val color: String?,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
