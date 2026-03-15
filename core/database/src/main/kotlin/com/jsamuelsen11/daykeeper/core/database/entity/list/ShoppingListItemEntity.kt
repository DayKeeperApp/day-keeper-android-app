package com.jsamuelsen11.daykeeper.core.database.entity.list

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "shopping_list_items",
  foreignKeys =
    [
      ForeignKey(
        entity = ShoppingListEntity::class,
        parentColumns = ["list_id"],
        childColumns = ["list_id"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index(value = ["list_id", "is_checked", "sort_order"])],
)
public data class ShoppingListItemEntity(
  @PrimaryKey @ColumnInfo(name = "item_id") val itemId: String,
  @ColumnInfo(name = "list_id") val listId: String,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "quantity") val quantity: Double = 1.0,
  @ColumnInfo(name = "unit") val unit: String?,
  @ColumnInfo(name = "is_checked") val isChecked: Boolean = false,
  @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
