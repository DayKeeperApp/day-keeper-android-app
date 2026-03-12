package com.jsamuelsen11.daykeeper.core.model.task

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule

/** A task, optionally belonging to a project. */
data class Task(
  val taskId: String,
  val projectId: String? = null,
  val spaceId: String,
  val tenantId: String,
  val title: String,
  val description: String? = null,
  val status: TaskStatus,
  val priority: Priority,
  val dueAt: Long? = null,
  val dueDate: String? = null,
  val recurrenceRule: RecurrenceRule? = null,
  val categoryId: String? = null,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
