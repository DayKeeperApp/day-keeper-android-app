package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.list.ShoppingList
import kotlinx.coroutines.flow.Flow

public interface ShoppingListRepository {
  public fun observeById(listId: String): Flow<ShoppingList?>

  public fun observeBySpace(spaceId: String): Flow<List<ShoppingList>>

  public suspend fun getById(listId: String): ShoppingList?

  public suspend fun upsert(list: ShoppingList)

  public suspend fun delete(listId: String)
}
