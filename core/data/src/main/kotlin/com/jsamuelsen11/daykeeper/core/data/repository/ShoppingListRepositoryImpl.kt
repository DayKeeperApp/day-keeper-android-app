package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.ShoppingListDao
import com.jsamuelsen11.daykeeper.core.model.list.ShoppingList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class ShoppingListRepositoryImpl(private val dao: ShoppingListDao) : ShoppingListRepository {
  public override fun observeAll(): Flow<List<ShoppingList>> =
    dao.observeAll().map { list -> list.map { it.toDomain() } }

  public override fun observeById(listId: String): Flow<ShoppingList?> =
    dao.observeById(listId).map { it?.toDomain() }

  public override fun observeBySpace(spaceId: String): Flow<List<ShoppingList>> =
    dao.observeBySpace(spaceId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(listId: String): ShoppingList? =
    dao.getById(listId)?.toDomain()

  public override suspend fun upsert(list: ShoppingList) {
    dao.upsert(list.toEntity())
  }

  public override suspend fun delete(listId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(listId, deletedAt = now, updatedAt = now)
  }
}
