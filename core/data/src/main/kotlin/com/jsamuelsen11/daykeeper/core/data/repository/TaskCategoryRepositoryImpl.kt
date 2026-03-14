package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.TaskCategoryDao
import com.jsamuelsen11.daykeeper.core.model.task.TaskCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class TaskCategoryRepositoryImpl(private val dao: TaskCategoryDao) : TaskCategoryRepository {
  public override fun observeAll(): Flow<List<TaskCategory>> =
    dao.observeAll().map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(categoryId: String): TaskCategory? =
    dao.getById(categoryId)?.toDomain()

  public override suspend fun upsert(category: TaskCategory) {
    dao.upsert(category.toEntity())
  }

  public override suspend fun delete(categoryId: String) {
    dao.hardDelete(categoryId)
  }
}
