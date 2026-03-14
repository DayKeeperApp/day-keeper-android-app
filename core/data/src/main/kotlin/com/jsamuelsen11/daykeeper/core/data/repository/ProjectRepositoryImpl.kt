package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.ProjectDao
import com.jsamuelsen11.daykeeper.core.model.task.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class ProjectRepositoryImpl(private val dao: ProjectDao) : ProjectRepository {
  public override fun observeById(projectId: String): Flow<Project?> =
    dao.observeById(projectId).map { it?.toDomain() }

  public override fun observeBySpace(spaceId: String): Flow<List<Project>> =
    dao.observeBySpace(spaceId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(projectId: String): Project? =
    dao.getById(projectId)?.toDomain()

  public override suspend fun upsert(project: Project) {
    dao.upsert(project.toEntity())
  }

  public override suspend fun delete(projectId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(projectId, deletedAt = now, updatedAt = now)
  }
}
