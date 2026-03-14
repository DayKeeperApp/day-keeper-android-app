package com.jsamuelsen11.daykeeper.core.data.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventReminderEntity
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule

public fun EventEntity.toDomain(): Event =
  Event(
    eventId = eventId,
    calendarId = calendarId,
    spaceId = spaceId,
    tenantId = tenantId,
    title = title,
    description = description,
    startAt = startAt,
    endAt = endAt,
    startDate = startDate,
    endDate = endDate,
    isAllDay = isAllDay,
    timezone = timezone,
    eventTypeId = eventTypeId,
    location = location,
    recurrenceRule = recurrenceRule?.let { RecurrenceRule.fromRruleString(it) },
    parentEventId = parentEventId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun Event.toEntity(): EventEntity =
  EventEntity(
    eventId = eventId,
    calendarId = calendarId,
    spaceId = spaceId,
    tenantId = tenantId,
    title = title,
    description = description,
    startAt = startAt,
    endAt = endAt,
    startDate = startDate,
    endDate = endDate,
    isAllDay = isAllDay,
    timezone = timezone,
    eventTypeId = eventTypeId,
    location = location,
    recurrenceRule = recurrenceRule?.toRruleString(),
    parentEventId = parentEventId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun EventReminderEntity.toDomain(): EventReminder =
  EventReminder(
    reminderId = reminderId,
    eventId = eventId,
    minutesBefore = minutesBefore,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun EventReminder.toEntity(): EventReminderEntity =
  EventReminderEntity(
    reminderId = reminderId,
    eventId = eventId,
    minutesBefore = minutesBefore,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )
