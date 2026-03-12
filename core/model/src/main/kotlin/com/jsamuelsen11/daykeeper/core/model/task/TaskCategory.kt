package com.jsamuelsen11.daykeeper.core.model.task

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A category that can be applied to tasks (system-defined or user-created). */
data class TaskCategory(
  val categoryId: String,
  val name: String,
  val normalizedName: String,
  val isSystem: Boolean,
  val color: String? = null,
  val createdAt: Long,
  val updatedAt: Long,
) : DayKeeperModel
