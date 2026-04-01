package com.jsamuelsen11.daykeeper.app

import android.app.Application
import androidx.work.Configuration
import com.jsamuelsen11.daykeeper.app.di.appModule
import com.jsamuelsen11.daykeeper.core.data.notification.NotificationChannelManager
import com.jsamuelsen11.daykeeper.core.data.sync.SyncScheduler
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class DayKeeperApplication : Application(), Configuration.Provider {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger()
      androidContext(this@DayKeeperApplication)
      workManagerFactory()
      modules(appModule)
    }
    get<NotificationChannelManager>().createChannels()
    get<SyncScheduler>().schedulePeriodicSync()
  }

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder().build()
}
