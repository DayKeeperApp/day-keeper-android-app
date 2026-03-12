package com.jsamuelsen11.daykeeper.core.model.calendar

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/**
 * A calendar event. Timed events use [startAt]/[endAt]; all-day events use [startDate]/[endDate].
 */
data class Event(
  val eventId: String,
  val calendarId: String,
  val spaceId: String,
  val tenantId: String,
  val title: String,
  val description: String? = null,
  val startAt: Long? = null,
  val endAt: Long? = null,
  val startDate: String? = null,
  val endDate: String? = null,
  val isAllDay: Boolean,
  val timezone: String,
  val eventTypeId: String? = null,
  val location: String? = null,
  val recurrenceRule: RecurrenceRule? = null,
  val parentEventId: String? = null,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
