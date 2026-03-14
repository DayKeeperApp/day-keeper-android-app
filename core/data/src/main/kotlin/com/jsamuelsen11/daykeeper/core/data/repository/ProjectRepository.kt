package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.task.Project
import kotlinx.coroutines.flow.Flow

public interface ProjectRepository {
  public fun observeById(projectId: String): Flow<Project?>

  public fun observeBySpace(spaceId: String): Flow<List<Project>>

  public suspend fun getById(projectId: String): Project?

  public suspend fun upsert(project: Project)

  public suspend fun delete(projectId: String)
}
