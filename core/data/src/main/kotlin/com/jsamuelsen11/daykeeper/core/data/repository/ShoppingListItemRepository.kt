package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem
import kotlinx.coroutines.flow.Flow

public interface ShoppingListItemRepository {
  public fun observeByList(listId: String): Flow<List<ShoppingListItem>>

  public suspend fun getById(itemId: String): ShoppingListItem?

  public suspend fun upsert(item: ShoppingListItem)

  public suspend fun delete(itemId: String)

  public suspend fun toggleChecked(itemId: String, isChecked: Boolean)
}
