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
import kotlinx.serialization.json.JsonObject

fun AccountEntity.toSyncJson(): JsonObject = jsonObj {
  put("tenantId", tenantId)
  put("displayName", displayName)
  put("email", email)
  put("timezone", timezone)
  put("weekStart", weekStart)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun SpaceEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", spaceId)
  put("tenantId", tenantId)
  put("name", name)
  put("normalizedName", normalizedName)
  put("type", type)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun SpaceMemberEntity.toSyncJson(): JsonObject = jsonObj {
  put("spaceId", spaceId)
  put("tenantId", tenantId)
  put("role", role)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun CalendarEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", calendarId)
  put("spaceId", spaceId)
  put("tenantId", tenantId)
  put("name", name)
  put("normalizedName", normalizedName)
  put("color", color)
  put("isDefault", isDefault)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun EventEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", eventId)
  put("calendarId", calendarId)
  put("spaceId", spaceId)
  put("tenantId", tenantId)
  put("title", title)
  putOrNull("description", description)
  putTimestampOrNull("startAt", startAt)
  putTimestampOrNull("endAt", endAt)
  putOrNull("startDate", startDate)
  putOrNull("endDate", endDate)
  put("isAllDay", isAllDay)
  put("timezone", timezone)
  putOrNull("eventTypeId", eventTypeId)
  putOrNull("location", location)
  putOrNull("recurrenceRule", recurrenceRule)
  putOrNull("parentEventId", parentEventId)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun EventTypeEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", eventTypeId)
  put("name", name)
  put("normalizedName", normalizedName)
  put("isSystem", isSystem)
  putOrNull("color", color)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
}

fun EventReminderEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", reminderId)
  put("calendarEventId", eventId)
  put("minutesBefore", minutesBefore)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun AttachmentEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", attachmentId)
  put("entityType", entityType)
  put("entityId", entityId)
  put("tenantId", tenantId)
  put("spaceId", spaceId)
  put("fileName", fileName)
  put("contentType", mimeType)
  put("fileSize", fileSize)
  putOrNull("remoteUrl", remoteUrl)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun DeviceEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", deviceId)
  put("tenantId", tenantId)
  put("deviceName", deviceName)
  putOrNull("fcmToken", fcmToken)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
}
