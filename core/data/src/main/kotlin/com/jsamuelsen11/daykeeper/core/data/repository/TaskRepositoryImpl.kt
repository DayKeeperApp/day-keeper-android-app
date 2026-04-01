package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.TaskDao
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class TaskRepositoryImpl(private val dao: TaskDao) : TaskRepository {
  public override fun observeById(taskId: String): Flow<Task?> =
    dao.observeById(taskId).map { it?.toDomain() }

  public override fun observeBySpace(spaceId: String): Flow<List<Task>> =
    dao.observeBySpace(spaceId).map { list -> list.map { it.toDomain() } }

  public override fun observeBySpaceAndStatus(
    spaceId: String,
    statuses: List<TaskStatus>,
  ): Flow<List<Task>> =
    dao.observeBySpaceAndStatus(spaceId, statuses.map { it.name }).map { list ->
      list.map { it.toDomain() }
    }

  public override fun observeByProject(projectId: String): Flow<List<Task>> =
    dao.observeByProject(projectId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(taskId: String): Task? = dao.getById(taskId)?.toDomain()

  public override suspend fun upsert(task: Task) {
    dao.upsert(task.toEntity())
  }

  public override suspend fun delete(taskId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(taskId, deletedAt = now, updatedAt = now)
  }

  public override suspend fun getTasksWithReminders(): List<Task> =
    dao.getTasksWithReminders().map { it.toDomain() }
}
