package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.list.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface ShoppingListItemDao {

  @Query(
    "SELECT * FROM shopping_list_items WHERE list_id = :listId" +
      " AND deleted_at IS NULL ORDER BY is_checked ASC, sort_order ASC"
  )
  public fun observeByList(listId: String): Flow<List<ShoppingListItemEntity>>

  @Query("SELECT * FROM shopping_list_items WHERE item_id = :itemId")
  public suspend fun getById(itemId: String): ShoppingListItemEntity?

  @Upsert public suspend fun upsert(entity: ShoppingListItemEntity)

  @Upsert public suspend fun upsertAll(entities: List<ShoppingListItemEntity>)

  @Query(
    "UPDATE shopping_list_items SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE item_id = :itemId"
  )
  public suspend fun softDelete(itemId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM shopping_list_items WHERE item_id = :itemId")
  public suspend fun hardDelete(itemId: String)

  @Query(
    "UPDATE shopping_list_items SET is_checked = :isChecked, updated_at = :updatedAt WHERE item_id = :itemId"
  )
  public suspend fun toggleChecked(itemId: String, isChecked: Boolean, updatedAt: Long)

  @Query("SELECT * FROM shopping_list_items WHERE updated_at > :since")
  public suspend fun getModifiedSince(since: Long): List<ShoppingListItemEntity>
}
