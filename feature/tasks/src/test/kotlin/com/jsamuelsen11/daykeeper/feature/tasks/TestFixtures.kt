package com.jsamuelsen11.daykeeper.feature.tasks

import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.Project
import com.jsamuelsen11.daykeeper.core.model.task.ProjectStatus
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskCategory
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus

internal const val TEST_TASK_ID = "test-task-id"
internal const val TEST_TASK_ID_2 = "test-task-id-2"
internal const val TEST_TASK_ID_3 = "test-task-id-3"
internal const val TEST_PROJECT_ID = "test-project-id"
internal const val TEST_PROJECT_ID_2 = "test-project-id-2"
internal const val TEST_CATEGORY_ID = "test-category-id"
internal const val TEST_CATEGORY_ID_2 = "test-category-id-2"
internal const val TEST_SPACE_ID = "default-space"
internal const val TEST_TENANT_ID = "default-tenant"
internal const val TEST_CREATED_AT = 1_000L
internal const val TEST_UPDATED_AT = 2_000L

internal fun makeTask(
  taskId: String = TEST_TASK_ID,
  projectId: String? = null,
  spaceId: String = TEST_SPACE_ID,
  tenantId: String = TEST_TENANT_ID,
  title: String = "Buy groceries",
  status: TaskStatus = TaskStatus.TODO,
  priority: Priority = Priority.NONE,
  categoryId: String? = null,
): Task =
  Task(
    taskId = taskId,
    projectId = projectId,
    spaceId = spaceId,
    tenantId = tenantId,
    title = title,
    status = status,
    priority = priority,
    categoryId = categoryId,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeProject(
  projectId: String = TEST_PROJECT_ID,
  spaceId: String = TEST_SPACE_ID,
  tenantId: String = TEST_TENANT_ID,
  name: String = "Work",
  status: ProjectStatus = ProjectStatus.ACTIVE,
): Project =
  Project(
    projectId = projectId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = name.lowercase().trim(),
    status = status,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeCategory(
  categoryId: String = TEST_CATEGORY_ID,
  name: String = "Personal",
  isSystem: Boolean = false,
  color: String? = "#FF0000",
): TaskCategory =
  TaskCategory(
    categoryId = categoryId,
    name = name,
    normalizedName = name.lowercase().trim(),
    isSystem = isSystem,
    color = color,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )
