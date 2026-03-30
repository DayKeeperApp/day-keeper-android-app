package com.jsamuelsen11.daykeeper.core.database.sync

import androidx.room.withTransaction
import com.jsamuelsen11.daykeeper.core.database.DayKeeperDatabase
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
import com.jsamuelsen11.daykeeper.core.database.entity.sync.SyncCursorEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.ProjectEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskCategoryEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskEntity

/**
 * Coordinates sync reads and writes across all entity DAOs. This is NOT a Room @Dao — it uses
 * [DayKeeperDatabase.withTransaction] directly for cross-entity atomic operations.
 */
class SyncDao(private val db: DayKeeperDatabase) {

  /** Collects all entities modified after [since] (epoch millis) across all tables. */
  suspend fun getChangedEntities(since: Long): ChangedEntities =
    ChangedEntities(
      accounts = db.accountDao().getModifiedSince(since),
      devices = db.deviceDao().getModifiedSince(since),
      spaces = db.spaceDao().getModifiedSince(since),
      spaceMembers = db.spaceMemberDao().getModifiedSince(since),
      calendars = db.calendarDao().getModifiedSince(since),
      eventTypes = db.eventTypeDao().getModifiedSince(since),
      events = db.eventDao().getModifiedSince(since),
      eventReminders = db.eventReminderDao().getModifiedSince(since),
      persons = db.personDao().getModifiedSince(since),
      contactMethods = db.contactMethodDao().getModifiedSince(since),
      addresses = db.addressDao().getModifiedSince(since),
      importantDates = db.importantDateDao().getModifiedSince(since),
      projects = db.projectDao().getModifiedSince(since),
      taskCategories = db.taskCategoryDao().getModifiedSince(since),
      tasks = db.taskDao().getModifiedSince(since),
      shoppingLists = db.shoppingListDao().getModifiedSince(since),
      shoppingListItems = db.shoppingListItemDao().getModifiedSince(since),
      attachments = db.attachmentDao().getModifiedSince(since),
    )

  /** Applies pulled changes and updates the sync cursor atomically. */
  suspend fun applyPulledChanges(changes: PulledChanges, cursor: SyncCursorEntity) {
    db.withTransaction {
      upsertAllChanges(changes)
      db.syncCursorDao().upsert(cursor)
    }
  }

  private suspend fun upsertAllChanges(c: PulledChanges) {
    upsertCoreChanges(c)
    upsertFeatureChanges(c)
  }

  private suspend fun upsertCoreChanges(c: PulledChanges) {
    db.accountDao().upsertAll(c.accounts)
    db.deviceDao().upsertAll(c.devices)
    db.spaceDao().upsertAll(c.spaces)
    db.spaceMemberDao().upsertAll(c.spaceMembers)
    db.calendarDao().upsertAll(c.calendars)
    db.eventTypeDao().upsertAll(c.eventTypes)
    db.eventDao().upsertAll(c.events)
    db.eventReminderDao().upsertAll(c.eventReminders)
    db.attachmentDao().upsertAll(c.attachments)
  }

  private suspend fun upsertFeatureChanges(c: PulledChanges) {
    db.personDao().upsertAll(c.persons)
    db.contactMethodDao().upsertAll(c.contactMethods)
    db.addressDao().upsertAll(c.addresses)
    db.importantDateDao().upsertAll(c.importantDates)
    db.projectDao().upsertAll(c.projects)
    db.taskCategoryDao().upsertAll(c.taskCategories)
    db.taskDao().upsertAll(c.tasks)
    db.shoppingListDao().upsertAll(c.shoppingLists)
    db.shoppingListItemDao().upsertAll(c.shoppingListItems)
  }
}

/** All locally modified entities since the last sync. */
data class ChangedEntities(
  val accounts: List<AccountEntity>,
  val devices: List<DeviceEntity>,
  val spaces: List<SpaceEntity>,
  val spaceMembers: List<SpaceMemberEntity>,
  val calendars: List<CalendarEntity>,
  val eventTypes: List<EventTypeEntity>,
  val events: List<EventEntity>,
  val eventReminders: List<EventReminderEntity>,
  val persons: List<PersonEntity>,
  val contactMethods: List<ContactMethodEntity>,
  val addresses: List<AddressEntity>,
  val importantDates: List<ImportantDateEntity>,
  val projects: List<ProjectEntity>,
  val taskCategories: List<TaskCategoryEntity>,
  val tasks: List<TaskEntity>,
  val shoppingLists: List<ShoppingListEntity>,
  val shoppingListItems: List<ShoppingListItemEntity>,
  val attachments: List<AttachmentEntity>,
)

/** Deserialized entities from pull response, ready for upsert. */
data class PulledChanges(
  val accounts: List<AccountEntity> = emptyList(),
  val devices: List<DeviceEntity> = emptyList(),
  val spaces: List<SpaceEntity> = emptyList(),
  val spaceMembers: List<SpaceMemberEntity> = emptyList(),
  val calendars: List<CalendarEntity> = emptyList(),
  val eventTypes: List<EventTypeEntity> = emptyList(),
  val events: List<EventEntity> = emptyList(),
  val eventReminders: List<EventReminderEntity> = emptyList(),
  val persons: List<PersonEntity> = emptyList(),
  val contactMethods: List<ContactMethodEntity> = emptyList(),
  val addresses: List<AddressEntity> = emptyList(),
  val importantDates: List<ImportantDateEntity> = emptyList(),
  val projects: List<ProjectEntity> = emptyList(),
  val taskCategories: List<TaskCategoryEntity> = emptyList(),
  val tasks: List<TaskEntity> = emptyList(),
  val shoppingLists: List<ShoppingListEntity> = emptyList(),
  val shoppingListItems: List<ShoppingListItemEntity> = emptyList(),
  val attachments: List<AttachmentEntity> = emptyList(),
)
