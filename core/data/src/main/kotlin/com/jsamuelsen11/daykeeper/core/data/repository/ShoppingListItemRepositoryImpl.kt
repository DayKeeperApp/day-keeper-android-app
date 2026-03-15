package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.ShoppingListItemDao
import com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class ShoppingListItemRepositoryImpl(private val dao: ShoppingListItemDao) :
  ShoppingListItemRepository {
  public override fun observeByList(listId: String): Flow<List<ShoppingListItem>> =
    dao.observeByList(listId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(itemId: String): ShoppingListItem? =
    dao.getById(itemId)?.toDomain()

  public override suspend fun upsert(item: ShoppingListItem) {
    dao.upsert(item.toEntity())
  }

  public override suspend fun delete(itemId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(itemId, deletedAt = now, updatedAt = now)
  }

  public override suspend fun toggleChecked(itemId: String, isChecked: Boolean) {
    dao.toggleChecked(itemId, isChecked, updatedAt = System.currentTimeMillis())
  }
}
