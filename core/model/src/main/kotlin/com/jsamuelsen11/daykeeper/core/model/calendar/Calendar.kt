package com.jsamuelsen11.daykeeper.core.model.calendar

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/**
 * A calendar within a space. Not to be confused with [java.util.Calendar]; this is a domain model
 * representing a user's named calendar.
 */
data class Calendar(
  val calendarId: String,
  val spaceId: String,
  val tenantId: String,
  val name: String,
  val normalizedName: String,
  val color: String,
  val isDefault: Boolean,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
