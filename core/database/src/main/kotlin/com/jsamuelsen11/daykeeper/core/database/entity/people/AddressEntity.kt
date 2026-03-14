package com.jsamuelsen11.daykeeper.core.database.entity.people

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "addresses",
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
public data class AddressEntity(
  @PrimaryKey @ColumnInfo(name = "address_id") val addressId: String,
  @ColumnInfo(name = "person_id") val personId: String,
  @ColumnInfo(name = "label") val label: String,
  @ColumnInfo(name = "street") val street: String?,
  @ColumnInfo(name = "city") val city: String?,
  @ColumnInfo(name = "state") val state: String?,
  @ColumnInfo(name = "postal_code") val postalCode: String?,
  @ColumnInfo(name = "country") val country: String?,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
