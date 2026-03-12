package com.jsamuelsen11.daykeeper.core.model.account

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A registered user account (tenant). */
data class Account(
  val tenantId: String,
  val displayName: String,
  val email: String,
  val timezone: String,
  val weekStart: WeekStart,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
