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
import com.jsamuelsen11.daykeeper.core.database.sync.PulledChanges
import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncChangeEntryDto

/** Parses [SyncChangeEntryDto] list into typed [PulledChanges] for Room upsert. */
object PullResponseParser {

  fun parse(entries: List<SyncChangeEntryDto>): PulledChanges {
    val builder = PulledChangesBuilder()
    entries
      .filter { it.data != null && EntitySerializer.isSupported(it.entityType) }
      .mapNotNull { EntitySerializer.jsonToEntity(it.entityType, it.data!!) }
      .forEach { builder.add(it) }
    return builder.build()
  }
}

/** Accumulates deserialized entities by type into a [PulledChanges] instance. */
internal class PulledChangesBuilder {
  private val accounts = mutableListOf<AccountEntity>()
  private val devices = mutableListOf<DeviceEntity>()
  private val spaces = mutableListOf<SpaceEntity>()
  private val spaceMembers = mutableListOf<SpaceMemberEntity>()
  private val calendars = mutableListOf<CalendarEntity>()
  private val eventTypes = mutableListOf<EventTypeEntity>()
  private val events = mutableListOf<EventEntity>()
  private val eventReminders = mutableListOf<EventReminderEntity>()
  private val persons = mutableListOf<PersonEntity>()
  private val contactMethods = mutableListOf<ContactMethodEntity>()
  private val addresses = mutableListOf<AddressEntity>()
  private val importantDates = mutableListOf<ImportantDateEntity>()
  private val projects = mutableListOf<ProjectEntity>()
  private val taskCategories = mutableListOf<TaskCategoryEntity>()
  private val tasks = mutableListOf<TaskEntity>()
  private val shoppingLists = mutableListOf<ShoppingListEntity>()
  private val shoppingListItems = mutableListOf<ShoppingListItemEntity>()
  private val attachments = mutableListOf<AttachmentEntity>()

  fun add(entity: Any) {
    addCoreEntity(entity) || addFeatureEntity(entity)
  }

  private fun addCoreEntity(entity: Any): Boolean =
    when (entity) {
      is AccountEntity -> accounts.add(entity)
      is DeviceEntity -> devices.add(entity)
      is SpaceEntity -> spaces.add(entity)
      is SpaceMemberEntity -> spaceMembers.add(entity)
      is CalendarEntity -> calendars.add(entity)
      is EventTypeEntity -> eventTypes.add(entity)
      is EventEntity -> events.add(entity)
      is EventReminderEntity -> eventReminders.add(entity)
      is AttachmentEntity -> attachments.add(entity)
      else -> false
    }

  private fun addFeatureEntity(entity: Any): Boolean =
    when (entity) {
      is PersonEntity -> persons.add(entity)
      is ContactMethodEntity -> contactMethods.add(entity)
      is AddressEntity -> addresses.add(entity)
      is ImportantDateEntity -> importantDates.add(entity)
      is ProjectEntity -> projects.add(entity)
      is TaskCategoryEntity -> taskCategories.add(entity)
      is TaskEntity -> tasks.add(entity)
      is ShoppingListEntity -> shoppingLists.add(entity)
      is ShoppingListItemEntity -> shoppingListItems.add(entity)
      else -> false
    }

  fun build(): PulledChanges =
    PulledChanges(
      accounts,
      devices,
      spaces,
      spaceMembers,
      calendars,
      eventTypes,
      events,
      eventReminders,
      persons,
      contactMethods,
      addresses,
      importantDates,
      projects,
      taskCategories,
      tasks,
      shoppingLists,
      shoppingListItems,
      attachments,
    )
}
