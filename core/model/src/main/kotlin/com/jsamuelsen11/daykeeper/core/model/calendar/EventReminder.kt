package com.jsamuelsen11.daykeeper.core.model.calendar

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A reminder associated with an event, triggered a set number of minutes before it starts. */
data class EventReminder(
  val reminderId: String,
  val eventId: String,
  val minutesBefore: Int,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
