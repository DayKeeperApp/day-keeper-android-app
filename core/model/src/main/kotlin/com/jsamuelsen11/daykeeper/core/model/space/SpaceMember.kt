package com.jsamuelsen11.daykeeper.core.model.space

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A user's membership and role within a space. */
data class SpaceMember(
  val spaceId: String,
  val tenantId: String,
  val role: SpaceRole,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
