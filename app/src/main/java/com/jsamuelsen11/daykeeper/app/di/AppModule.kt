package com.jsamuelsen11.daykeeper.app.di

import androidx.work.WorkManager
import com.jsamuelsen11.daykeeper.app.BuildConfig
import com.jsamuelsen11.daykeeper.core.data.di.dataModule
import com.jsamuelsen11.daykeeper.core.data.sync.SyncScheduler
import com.jsamuelsen11.daykeeper.core.database.di.databaseModule
import com.jsamuelsen11.daykeeper.core.network.config.SyncConfig
import com.jsamuelsen11.daykeeper.core.network.di.networkModule
import com.jsamuelsen11.daykeeper.feature.calendar.di.calendarModule
import com.jsamuelsen11.daykeeper.feature.lists.di.listsModule
import com.jsamuelsen11.daykeeper.feature.people.di.peopleModule
import com.jsamuelsen11.daykeeper.feature.tasks.di.tasksModule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
  includes(
    databaseModule,
    dataModule,
    networkModule,
    calendarModule,
    listsModule,
    peopleModule,
    tasksModule,
  )

  single { SyncConfig(baseUrl = BuildConfig.SYNC_BASE_URL) }
  single { WorkManager.getInstance(androidContext()) }
  single { SyncScheduler(get()) }
}
