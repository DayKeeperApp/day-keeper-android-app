package com.jsamuelsen11.daykeeper.app

import android.app.Application
import com.jsamuelsen11.daykeeper.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class DayKeeperApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger()
      androidContext(this@DayKeeperApplication)
      modules(appModule)
    }
  }
}
