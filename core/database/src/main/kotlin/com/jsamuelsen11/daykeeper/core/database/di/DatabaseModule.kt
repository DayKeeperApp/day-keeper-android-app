package com.jsamuelsen11.daykeeper.core.database.di

import androidx.room.Room
import com.jsamuelsen11.daykeeper.core.database.DayKeeperDatabase
import com.jsamuelsen11.daykeeper.core.database.sync.SyncDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

public val databaseModule = module {
  single {
    Room.databaseBuilder(
        androidContext(),
        DayKeeperDatabase::class.java,
        DayKeeperDatabase.DATABASE_NAME,
      )
      .fallbackToDestructiveMigration()
      .build()
  }

  single { get<DayKeeperDatabase>().accountDao() }
  single { get<DayKeeperDatabase>().deviceDao() }
  single { get<DayKeeperDatabase>().spaceDao() }
  single { get<DayKeeperDatabase>().spaceMemberDao() }
  single { get<DayKeeperDatabase>().calendarDao() }
  single { get<DayKeeperDatabase>().eventTypeDao() }
  single { get<DayKeeperDatabase>().eventDao() }
  single { get<DayKeeperDatabase>().eventReminderDao() }
  single { get<DayKeeperDatabase>().personDao() }
  single { get<DayKeeperDatabase>().contactMethodDao() }
  single { get<DayKeeperDatabase>().addressDao() }
  single { get<DayKeeperDatabase>().importantDateDao() }
  single { get<DayKeeperDatabase>().projectDao() }
  single { get<DayKeeperDatabase>().taskCategoryDao() }
  single { get<DayKeeperDatabase>().taskDao() }
  single { get<DayKeeperDatabase>().shoppingListDao() }
  single { get<DayKeeperDatabase>().shoppingListItemDao() }
  single { get<DayKeeperDatabase>().attachmentDao() }
  single { get<DayKeeperDatabase>().syncCursorDao() }
  single { SyncDao(get()) }
}
