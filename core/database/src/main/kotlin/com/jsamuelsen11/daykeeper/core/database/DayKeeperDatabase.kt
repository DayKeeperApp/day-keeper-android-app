package com.jsamuelsen11.daykeeper.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jsamuelsen11.daykeeper.core.database.dao.AccountDao
import com.jsamuelsen11.daykeeper.core.database.dao.AddressDao
import com.jsamuelsen11.daykeeper.core.database.dao.AttachmentDao
import com.jsamuelsen11.daykeeper.core.database.dao.CalendarDao
import com.jsamuelsen11.daykeeper.core.database.dao.ContactMethodDao
import com.jsamuelsen11.daykeeper.core.database.dao.DeviceDao
import com.jsamuelsen11.daykeeper.core.database.dao.EventDao
import com.jsamuelsen11.daykeeper.core.database.dao.EventReminderDao
import com.jsamuelsen11.daykeeper.core.database.dao.EventTypeDao
import com.jsamuelsen11.daykeeper.core.database.dao.ImportantDateDao
import com.jsamuelsen11.daykeeper.core.database.dao.PersonDao
import com.jsamuelsen11.daykeeper.core.database.dao.ProjectDao
import com.jsamuelsen11.daykeeper.core.database.dao.ShoppingListDao
import com.jsamuelsen11.daykeeper.core.database.dao.ShoppingListItemDao
import com.jsamuelsen11.daykeeper.core.database.dao.SpaceDao
import com.jsamuelsen11.daykeeper.core.database.dao.SpaceMemberDao
import com.jsamuelsen11.daykeeper.core.database.dao.SyncCursorDao
import com.jsamuelsen11.daykeeper.core.database.dao.TaskCategoryDao
import com.jsamuelsen11.daykeeper.core.database.dao.TaskDao
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

@Database(
  entities =
    [
      AccountEntity::class,
      DeviceEntity::class,
      SpaceEntity::class,
      SpaceMemberEntity::class,
      CalendarEntity::class,
      EventTypeEntity::class,
      EventEntity::class,
      EventReminderEntity::class,
      PersonEntity::class,
      ContactMethodEntity::class,
      AddressEntity::class,
      ImportantDateEntity::class,
      ProjectEntity::class,
      TaskCategoryEntity::class,
      TaskEntity::class,
      ShoppingListEntity::class,
      ShoppingListItemEntity::class,
      AttachmentEntity::class,
      SyncCursorEntity::class,
    ],
  version = 2,
  exportSchema = true,
)
public abstract class DayKeeperDatabase : RoomDatabase() {

  public abstract fun accountDao(): AccountDao

  public abstract fun deviceDao(): DeviceDao

  public abstract fun spaceDao(): SpaceDao

  public abstract fun spaceMemberDao(): SpaceMemberDao

  public abstract fun calendarDao(): CalendarDao

  public abstract fun eventTypeDao(): EventTypeDao

  public abstract fun eventDao(): EventDao

  public abstract fun eventReminderDao(): EventReminderDao

  public abstract fun personDao(): PersonDao

  public abstract fun contactMethodDao(): ContactMethodDao

  public abstract fun addressDao(): AddressDao

  public abstract fun importantDateDao(): ImportantDateDao

  public abstract fun projectDao(): ProjectDao

  public abstract fun taskCategoryDao(): TaskCategoryDao

  public abstract fun taskDao(): TaskDao

  public abstract fun shoppingListDao(): ShoppingListDao

  public abstract fun shoppingListItemDao(): ShoppingListItemDao

  public abstract fun attachmentDao(): AttachmentDao

  public abstract fun syncCursorDao(): SyncCursorDao

  public companion object {
    public const val DATABASE_NAME: String = "daykeeper.db"
  }
}
