package com.jsamuelsen11.daykeeper.core.database.entity.people

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "important_dates",
  foreignKeys =
    [
      ForeignKey(
        entity = PersonEntity::class,
        parentColumns = ["person_id"],
        childColumns = ["person_id"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index(value = ["person_id"])],
)
public data class ImportantDateEntity(
  @PrimaryKey @ColumnInfo(name = "important_date_id") val importantDateId: String,
  @ColumnInfo(name = "person_id") val personId: String,
  @ColumnInfo(name = "label") val label: String,
  @ColumnInfo(name = "date") val date: String,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
