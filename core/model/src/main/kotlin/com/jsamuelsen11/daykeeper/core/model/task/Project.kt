package com.jsamuelsen11.daykeeper.core.model.task

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A project that groups related tasks within a space. */
data class Project(
  val projectId: String,
  val spaceId: String,
  val tenantId: String,
  val name: String,
  val normalizedName: String,
  val description: String? = null,
  val status: ProjectStatus,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
