package com.jsamuelsen11.daykeeper.feature.calendar

import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import com.jsamuelsen11.daykeeper.core.model.calendar.EventType

internal const val TEST_EVENT_ID = "test-event-id"
internal const val TEST_EVENT_ID_2 = "test-event-id-2"
internal const val TEST_CALENDAR_ID = "test-calendar-id"
internal const val TEST_CALENDAR_ID_2 = "test-calendar-id-2"
internal const val TEST_EVENT_TYPE_ID = "test-event-type-id"
internal const val TEST_REMINDER_ID = "test-reminder-id"
internal const val TEST_SPACE_ID = "default-space"
internal const val TEST_TENANT_ID = "default-tenant"
internal const val TEST_CREATED_AT = 1_000L
internal const val TEST_UPDATED_AT = 2_000L

// A fixed epoch-millis timestamp: 2024-03-21T10:26:40Z
internal const val TEST_START_AT = 1_711_017_600_000L

// TEST_START_AT + 1 hour
internal const val TEST_END_AT = 1_711_021_200_000L

internal fun makeEvent(
  eventId: String = TEST_EVENT_ID,
  calendarId: String = TEST_CALENDAR_ID,
  title: String = "Team Meeting",
  isAllDay: Boolean = false,
  startAt: Long? = TEST_START_AT,
  endAt: Long? = TEST_END_AT,
  startDate: String? = null,
  endDate: String? = null,
  timezone: String = "UTC",
  eventTypeId: String? = null,
  location: String? = null,
  deletedAt: Long? = null,
): Event =
  Event(
    eventId = eventId,
    calendarId = calendarId,
    spaceId = TEST_SPACE_ID,
    tenantId = TEST_TENANT_ID,
    title = title,
    isAllDay = isAllDay,
    startAt = startAt,
    endAt = endAt,
    startDate = startDate,
    endDate = endDate,
    timezone = timezone,
    eventTypeId = eventTypeId,
    location = location,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
    deletedAt = deletedAt,
  )

internal fun makeCalendar(
  calendarId: String = TEST_CALENDAR_ID,
  name: String = "Work",
  color: String = "#4285F4",
  isDefault: Boolean = true,
  deletedAt: Long? = null,
): Calendar =
  Calendar(
    calendarId = calendarId,
    spaceId = TEST_SPACE_ID,
    tenantId = TEST_TENANT_ID,
    name = name,
    normalizedName = name.lowercase().trim(),
    color = color,
    isDefault = isDefault,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
    deletedAt = deletedAt,
  )

internal fun makeEventType(
  eventTypeId: String = TEST_EVENT_TYPE_ID,
  name: String = "Meeting",
  isSystem: Boolean = false,
  color: String? = null,
): EventType =
  EventType(
    eventTypeId = eventTypeId,
    name = name,
    normalizedName = name.lowercase().trim(),
    isSystem = isSystem,
    color = color,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeEventReminder(
  reminderId: String = TEST_REMINDER_ID,
  eventId: String = TEST_EVENT_ID,
  minutesBefore: Int = 15,
  deletedAt: Long? = null,
): EventReminder =
  EventReminder(
    reminderId = reminderId,
    eventId = eventId,
    minutesBefore = minutesBefore,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
    deletedAt = deletedAt,
  )
