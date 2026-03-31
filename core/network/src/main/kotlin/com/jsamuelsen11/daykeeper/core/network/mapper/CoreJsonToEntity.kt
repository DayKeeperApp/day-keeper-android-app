package com.jsamuelsen11.daykeeper.core.network.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity
import com.jsamuelsen11.daykeeper.core.database.entity.account.DeviceEntity
import com.jsamuelsen11.daykeeper.core.database.entity.attachment.AttachmentEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.CalendarEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventReminderEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventTypeEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceMemberEntity
import kotlinx.serialization.json.JsonElement

fun JsonElement.toAccountEntity(): AccountEntity = obj { o ->
  AccountEntity(
    tenantId = o.str("tenantId"),
    displayName = o.str("displayName"),
    email = o.str("email"),
    timezone = o.str("timezone"),
    weekStart = o.str("weekStart"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toSpaceEntity(): SpaceEntity = obj { o ->
  SpaceEntity(
    spaceId = o.str("id"),
    tenantId = o.str("tenantId"),
    name = o.str("name"),
    normalizedName = o.str("normalizedName"),
    type = o.str("type"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toSpaceMemberEntity(): SpaceMemberEntity = obj { o ->
  SpaceMemberEntity(
    spaceId = o.str("spaceId"),
    tenantId = o.str("tenantId"),
    role = o.str("role"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toCalendarEntity(): CalendarEntity = obj { o ->
  CalendarEntity(
    calendarId = o.str("id"),
    spaceId = o.str("spaceId"),
    tenantId = o.str("tenantId"),
    name = o.str("name"),
    normalizedName = o.str("normalizedName"),
    color = o.str("color"),
    isDefault = o.bool("isDefault"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toEventEntity(): EventEntity = obj { o ->
  EventEntity(
    eventId = o.str("id"),
    calendarId = o.str("calendarId"),
    spaceId = o.str("spaceId"),
    tenantId = o.str("tenantId"),
    title = o.str("title"),
    description = o.strOrNull("description"),
    startAt = o.epochMsOrNull("startAt"),
    endAt = o.epochMsOrNull("endAt"),
    startDate = o.strOrNull("startDate"),
    endDate = o.strOrNull("endDate"),
    isAllDay = o.bool("isAllDay"),
    timezone = o.str("timezone"),
    eventTypeId = o.strOrNull("eventTypeId"),
    location = o.strOrNull("location"),
    recurrenceRule = o.strOrNull("recurrenceRule"),
    parentEventId = o.strOrNull("parentEventId"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toEventTypeEntity(): EventTypeEntity = obj { o ->
  EventTypeEntity(
    eventTypeId = o.str("id"),
    name = o.str("name"),
    normalizedName = o.str("normalizedName"),
    isSystem = o.bool("isSystem"),
    color = o.strOrNull("color"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
  )
}

fun JsonElement.toEventReminderEntity(): EventReminderEntity = obj { o ->
  EventReminderEntity(
    reminderId = o.str("id"),
    eventId = o.str("calendarEventId"),
    minutesBefore = o.int("minutesBefore"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toAttachmentEntity(): AttachmentEntity = obj { o ->
  AttachmentEntity(
    attachmentId = o.str("id"),
    entityType = o.str("entityType"),
    entityId = o.str("entityId"),
    tenantId = o.str("tenantId"),
    spaceId = o.str("spaceId"),
    fileName = o.str("fileName"),
    mimeType = o.str("contentType"),
    fileSize = o.long("fileSize"),
    remoteUrl = o.strOrNull("remoteUrl"),
    localPath = null,
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toDeviceEntity(): DeviceEntity = obj { o ->
  DeviceEntity(
    deviceId = o.str("id"),
    tenantId = o.str("tenantId"),
    deviceName = o.str("deviceName"),
    fcmToken = o.strOrNull("fcmToken"),
    lastSyncCursor = o.longOrNull("lastSyncAt") ?: 0L,
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
  )
}
