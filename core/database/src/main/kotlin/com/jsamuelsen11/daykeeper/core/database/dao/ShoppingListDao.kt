package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.list.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface ShoppingListDao {

  @Query("SELECT * FROM shopping_lists WHERE list_id = :listId AND deleted_at IS NULL")
  public fun observeById(listId: String): Flow<ShoppingListEntity?>

  @Query("SELECT * FROM shopping_lists WHERE space_id = :spaceId AND deleted_at IS NULL")
  public fun observeBySpace(spaceId: String): Flow<List<ShoppingListEntity>>

  @Query("SELECT * FROM shopping_lists WHERE list_id = :listId")
  public suspend fun getById(listId: String): ShoppingListEntity?

  @Upsert public suspend fun upsert(entity: ShoppingListEntity)

  @Upsert public suspend fun upsertAll(entities: List<ShoppingListEntity>)

  @Query(
    "UPDATE shopping_lists SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE list_id = :listId"
  )
  public suspend fun softDelete(listId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM shopping_lists WHERE list_id = :listId")
  public suspend fun hardDelete(listId: String)
}
