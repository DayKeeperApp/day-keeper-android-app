package com.jsamuelsen11.daykeeper.core.data.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.calendar.CalendarEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventTypeEntity
import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar
import com.jsamuelsen11.daykeeper.core.model.calendar.EventType

public fun CalendarEntity.toDomain(): Calendar =
  Calendar(
    calendarId = calendarId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = normalizedName,
    color = color,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun Calendar.toEntity(): CalendarEntity =
  CalendarEntity(
    calendarId = calendarId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = normalizedName,
    color = color,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun EventTypeEntity.toDomain(): EventType =
  EventType(
    eventTypeId = eventTypeId,
    name = name,
    normalizedName = normalizedName,
    isSystem = isSystem,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )

public fun EventType.toEntity(): EventTypeEntity =
  EventTypeEntity(
    eventTypeId = eventTypeId,
    name = name,
    normalizedName = normalizedName,
    isSystem = isSystem,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )
