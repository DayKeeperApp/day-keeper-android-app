package com.jsamuelsen11.daykeeper.core.data.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.task.ProjectEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskCategoryEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskEntity
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.Project
import com.jsamuelsen11.daykeeper.core.model.task.ProjectStatus
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskCategory
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus

public fun ProjectEntity.toDomain(): Project =
  Project(
    projectId = projectId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = normalizedName,
    description = description,
    status = ProjectStatus.valueOf(status),
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun Project.toEntity(): ProjectEntity =
  ProjectEntity(
    projectId = projectId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = normalizedName,
    description = description,
    status = status.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun TaskCategoryEntity.toDomain(): TaskCategory =
  TaskCategory(
    categoryId = categoryId,
    name = name,
    normalizedName = normalizedName,
    isSystem = isSystem,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )

public fun TaskCategory.toEntity(): TaskCategoryEntity =
  TaskCategoryEntity(
    categoryId = categoryId,
    name = name,
    normalizedName = normalizedName,
    isSystem = isSystem,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )

public fun TaskEntity.toDomain(): Task =
  Task(
    taskId = taskId,
    projectId = projectId,
    spaceId = spaceId,
    tenantId = tenantId,
    title = title,
    description = description,
    status = TaskStatus.valueOf(status),
    priority = Priority.valueOf(priority),
    dueAt = dueAt,
    dueDate = dueDate,
    recurrenceRule = recurrenceRule?.let { RecurrenceRule.fromRruleString(it) },
    categoryId = categoryId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun Task.toEntity(): TaskEntity =
  TaskEntity(
    taskId = taskId,
    projectId = projectId,
    spaceId = spaceId,
    tenantId = tenantId,
    title = title,
    description = description,
    status = status.name,
    priority = priority.name,
    dueAt = dueAt,
    dueDate = dueDate,
    recurrenceRule = recurrenceRule?.toRruleString(),
    categoryId = categoryId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )
