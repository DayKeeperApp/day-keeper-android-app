package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.task.TaskCategory
import kotlinx.coroutines.flow.Flow

public interface TaskCategoryRepository {
  public fun observeAll(): Flow<List<TaskCategory>>

  public suspend fun getById(categoryId: String): TaskCategory?

  public suspend fun upsert(category: TaskCategory)

  public suspend fun delete(categoryId: String)
}
