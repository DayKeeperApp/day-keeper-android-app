package com.jsamuelsen11.daykeeper.core.model.calendar

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A category for events (e.g. "Meeting", "Appointment", "Holiday"). */
data class EventType(
  val eventTypeId: String,
  val name: String,
  val normalizedName: String,
  val isSystem: Boolean,
  val color: String? = null,
  val createdAt: Long,
  val updatedAt: Long,
) : DayKeeperModel
