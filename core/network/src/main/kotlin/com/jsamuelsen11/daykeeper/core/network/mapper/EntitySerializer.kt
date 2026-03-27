package com.jsamuelsen11.daykeeper.core.network.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity
import com.jsamuelsen11.daykeeper.core.database.entity.account.DeviceEntity
import com.jsamuelsen11.daykeeper.core.database.entity.attachment.AttachmentEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.CalendarEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventReminderEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventTypeEntity
import com.jsamuelsen11.daykeeper.core.database.entity.list.ShoppingListEntity
import com.jsamuelsen11.daykeeper.core.database.entity.list.ShoppingListItemEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.AddressEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.ContactMethodEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.ImportantDateEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.PersonEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceMemberEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.ProjectEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskCategoryEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskEntity
import com.jsamuelsen11.daykeeper.core.network.dto.sync.ChangeLogEntityType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * Dispatches entity ↔ JSON conversion based on [ChangeLogEntityType].
 *
 * Serialization (entity → JSON) is in [EntityToJsonMapper]. Deserialization (JSON → entity) is in
 * [JsonToEntityMapper]. JSON DSL helpers are in [JsonDsl].
 */
object EntitySerializer {

  private val serializers: Map<ChangeLogEntityType, (Any) -> JsonElement> =
    mapOf(
      ChangeLogEntityType.TENANT to { (it as AccountEntity).toSyncJson() },
      ChangeLogEntityType.SPACE to { (it as SpaceEntity).toSyncJson() },
      ChangeLogEntityType.SPACE_MEMBERSHIP to { (it as SpaceMemberEntity).toSyncJson() },
      ChangeLogEntityType.CALENDAR to { (it as CalendarEntity).toSyncJson() },
      ChangeLogEntityType.CALENDAR_EVENT to { (it as EventEntity).toSyncJson() },
      ChangeLogEntityType.EVENT_TYPE to { (it as EventTypeEntity).toSyncJson() },
      ChangeLogEntityType.EVENT_REMINDER to { (it as EventReminderEntity).toSyncJson() },
      ChangeLogEntityType.TASK_ITEM to { (it as TaskEntity).toSyncJson() },
      ChangeLogEntityType.TASK_CATEGORY to { (it as TaskCategoryEntity).toSyncJson() },
      ChangeLogEntityType.PROJECT to { (it as ProjectEntity).toSyncJson() },
      ChangeLogEntityType.PERSON to { (it as PersonEntity).toSyncJson() },
      ChangeLogEntityType.CONTACT_METHOD to { (it as ContactMethodEntity).toSyncJson() },
      ChangeLogEntityType.ADDRESS to { (it as AddressEntity).toSyncJson() },
      ChangeLogEntityType.IMPORTANT_DATE to { (it as ImportantDateEntity).toSyncJson() },
      ChangeLogEntityType.SHOPPING_LIST to { (it as ShoppingListEntity).toSyncJson() },
      ChangeLogEntityType.LIST_ITEM to { (it as ShoppingListItemEntity).toSyncJson() },
      ChangeLogEntityType.ATTACHMENT to { (it as AttachmentEntity).toSyncJson() },
      ChangeLogEntityType.DEVICE to { (it as DeviceEntity).toSyncJson() },
    )

  private val deserializers: Map<ChangeLogEntityType, (JsonElement) -> Any> =
    mapOf(
      ChangeLogEntityType.TENANT to { it.toAccountEntity() },
      ChangeLogEntityType.SPACE to { it.toSpaceEntity() },
      ChangeLogEntityType.SPACE_MEMBERSHIP to { it.toSpaceMemberEntity() },
      ChangeLogEntityType.CALENDAR to { it.toCalendarEntity() },
      ChangeLogEntityType.CALENDAR_EVENT to { it.toEventEntity() },
      ChangeLogEntityType.EVENT_TYPE to { it.toEventTypeEntity() },
      ChangeLogEntityType.EVENT_REMINDER to { it.toEventReminderEntity() },
      ChangeLogEntityType.TASK_ITEM to { it.toTaskEntity() },
      ChangeLogEntityType.TASK_CATEGORY to { it.toTaskCategoryEntity() },
      ChangeLogEntityType.PROJECT to { it.toProjectEntity() },
      ChangeLogEntityType.PERSON to { it.toPersonEntity() },
      ChangeLogEntityType.CONTACT_METHOD to { it.toContactMethodEntity() },
      ChangeLogEntityType.ADDRESS to { it.toAddressEntity() },
      ChangeLogEntityType.IMPORTANT_DATE to { it.toImportantDateEntity() },
      ChangeLogEntityType.SHOPPING_LIST to { it.toShoppingListEntity() },
      ChangeLogEntityType.LIST_ITEM to { it.toShoppingListItemEntity() },
      ChangeLogEntityType.ATTACHMENT to { it.toAttachmentEntity() },
      ChangeLogEntityType.DEVICE to { it.toDeviceEntity() },
    )

  fun entityToJson(entityType: ChangeLogEntityType, entity: Any): JsonElement =
    serializers[entityType]?.invoke(entity) ?: JsonNull

  fun jsonToEntity(entityType: ChangeLogEntityType, json: JsonElement): Any? =
    deserializers[entityType]?.invoke(json)

  fun isSupported(entityType: ChangeLogEntityType): Boolean = entityType in serializers
}
