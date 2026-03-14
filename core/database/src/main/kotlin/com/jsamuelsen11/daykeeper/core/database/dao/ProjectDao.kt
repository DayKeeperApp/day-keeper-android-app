package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.task.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface ProjectDao {

  @Query("SELECT * FROM projects WHERE project_id = :projectId AND deleted_at IS NULL")
  public fun observeById(projectId: String): Flow<ProjectEntity?>

  @Query("SELECT * FROM projects WHERE space_id = :spaceId AND deleted_at IS NULL")
  public fun observeBySpace(spaceId: String): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects WHERE project_id = :projectId")
  public suspend fun getById(projectId: String): ProjectEntity?

  @Upsert public suspend fun upsert(entity: ProjectEntity)

  @Upsert public suspend fun upsertAll(entities: List<ProjectEntity>)

  @Query(
    "UPDATE projects SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE project_id = :projectId"
  )
  public suspend fun softDelete(projectId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM projects WHERE project_id = :projectId")
  public suspend fun hardDelete(projectId: String)
}
