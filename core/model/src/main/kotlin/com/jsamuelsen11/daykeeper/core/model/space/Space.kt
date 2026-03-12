package com.jsamuelsen11.daykeeper.core.model.space

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A sharing boundary that groups related entities. */
data class Space(
  val spaceId: String,
  val tenantId: String,
  val name: String,
  val normalizedName: String,
  val type: SpaceType,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
