package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface TaskDao {

  @Query("SELECT * FROM tasks WHERE task_id = :taskId AND deleted_at IS NULL")
  public fun observeById(taskId: String): Flow<TaskEntity?>

  @Query("SELECT * FROM tasks WHERE space_id = :spaceId AND deleted_at IS NULL")
  public fun observeBySpace(spaceId: String): Flow<List<TaskEntity>>

  @Query(
    "SELECT * FROM tasks WHERE space_id = :spaceId AND status IN (:statuses) AND deleted_at IS NULL ORDER BY due_at ASC"
  )
  public fun observeBySpaceAndStatus(
    spaceId: String,
    statuses: List<String>,
  ): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE project_id = :projectId AND deleted_at IS NULL")
  public fun observeByProject(projectId: String): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE task_id = :taskId")
  public suspend fun getById(taskId: String): TaskEntity?

  @Upsert public suspend fun upsert(entity: TaskEntity)

  @Upsert public suspend fun upsertAll(entities: List<TaskEntity>)

  @Query(
    "UPDATE tasks SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE task_id = :taskId"
  )
  public suspend fun softDelete(taskId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM tasks WHERE task_id = :taskId") public suspend fun hardDelete(taskId: String)
}
