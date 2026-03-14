package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import kotlinx.coroutines.flow.Flow

public interface TaskRepository {
  public fun observeById(taskId: String): Flow<Task?>

  public fun observeBySpace(spaceId: String): Flow<List<Task>>

  public fun observeBySpaceAndStatus(spaceId: String, statuses: List<TaskStatus>): Flow<List<Task>>

  public fun observeByProject(projectId: String): Flow<List<Task>>

  public suspend fun getById(taskId: String): Task?

  public suspend fun upsert(task: Task)

  public suspend fun delete(taskId: String)
}
