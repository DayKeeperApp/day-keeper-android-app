package com.jsamuelsen11.daykeeper.core.network.mapper

import com.jsamuelsen11.daykeeper.core.database.sync.ChangedEntities
import com.jsamuelsen11.daykeeper.core.network.dto.sync.ChangeLogEntityType
import com.jsamuelsen11.daykeeper.core.network.dto.sync.ChangeOperation
import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPushEntryDto

/** Builds [SyncPushEntryDto] list from locally changed entities. */
object SyncEntryBuilder {

  fun buildPushEntries(changed: ChangedEntities): List<SyncPushEntryDto> = buildList {
    addEntries(ChangeLogEntityType.TENANT, changed.accounts) { it.tenantId to it.deletedAt }
    addEntries(ChangeLogEntityType.DEVICE, changed.devices) { it.deviceId to null }
    addEntries(ChangeLogEntityType.SPACE, changed.spaces) { it.spaceId to it.deletedAt }
    addEntries(ChangeLogEntityType.SPACE_MEMBERSHIP, changed.spaceMembers) {
      "${it.spaceId}:${it.tenantId}" to it.deletedAt
    }
    addEntries(ChangeLogEntityType.CALENDAR, changed.calendars) { it.calendarId to it.deletedAt }
    addEntries(ChangeLogEntityType.EVENT_TYPE, changed.eventTypes) { it.eventTypeId to null }
    addEntries(ChangeLogEntityType.CALENDAR_EVENT, changed.events) { it.eventId to it.deletedAt }
    addEntries(ChangeLogEntityType.EVENT_REMINDER, changed.eventReminders) {
      it.reminderId to it.deletedAt
    }
    addEntries(ChangeLogEntityType.PERSON, changed.persons) { it.personId to it.deletedAt }
    addEntries(ChangeLogEntityType.CONTACT_METHOD, changed.contactMethods) {
      it.contactMethodId to it.deletedAt
    }
    addEntries(ChangeLogEntityType.ADDRESS, changed.addresses) { it.addressId to it.deletedAt }
    addEntries(ChangeLogEntityType.IMPORTANT_DATE, changed.importantDates) {
      it.importantDateId to it.deletedAt
    }
    addEntries(ChangeLogEntityType.PROJECT, changed.projects) { it.projectId to it.deletedAt }
    addEntries(ChangeLogEntityType.TASK_CATEGORY, changed.taskCategories) { it.categoryId to null }
    addEntries(ChangeLogEntityType.TASK_ITEM, changed.tasks) { it.taskId to it.deletedAt }
    addEntries(ChangeLogEntityType.SHOPPING_LIST, changed.shoppingLists) {
      it.listId to it.deletedAt
    }
    addEntries(ChangeLogEntityType.LIST_ITEM, changed.shoppingListItems) {
      it.itemId to it.deletedAt
    }
    addEntries(ChangeLogEntityType.ATTACHMENT, changed.attachments) {
      it.attachmentId to it.deletedAt
    }
  }

  private inline fun <T : Any> MutableList<SyncPushEntryDto>.addEntries(
    entityType: ChangeLogEntityType,
    entities: List<T>,
    idAndDeleted: (T) -> Pair<String, Long?>,
  ) {
    for (entity in entities) {
      val (entityId, deletedAt) = idAndDeleted(entity)
      val operation = if (deletedAt != null) ChangeOperation.DELETED else ChangeOperation.UPDATED
      val timestamp = epochMsToIso(System.currentTimeMillis())
      val data = EntitySerializer.entityToJson(entityType, entity)
      add(SyncPushEntryDto(entityType, entityId, operation, timestamp, data))
    }
  }
}
