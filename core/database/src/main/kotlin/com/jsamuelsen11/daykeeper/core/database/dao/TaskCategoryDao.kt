package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface TaskCategoryDao {

  @Query("SELECT * FROM task_categories") public fun observeAll(): Flow<List<TaskCategoryEntity>>

  @Query("SELECT * FROM task_categories WHERE category_id = :categoryId")
  public suspend fun getById(categoryId: String): TaskCategoryEntity?

  @Upsert public suspend fun upsert(entity: TaskCategoryEntity)

  @Upsert public suspend fun upsertAll(entities: List<TaskCategoryEntity>)

  @Query("DELETE FROM task_categories WHERE category_id = :categoryId")
  public suspend fun hardDelete(categoryId: String)
}
