package com.jsamuelsen11.daykeeper.core.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/** Creates and manages notification channels for the Day Keeper app. */
public class NotificationChannelManager(private val context: Context) {

  /** Creates all notification channels. Safe to call repeatedly (idempotent). */
  public fun createChannels() {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannels(
      listOf(
        NotificationChannel(
            CHANNEL_REMINDERS,
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH,
          )
          .apply { description = "Event and task reminders" },
        NotificationChannel(
            CHANNEL_SYNC,
            "Sync Updates",
            NotificationManager.IMPORTANCE_LOW,
          )
          .apply { description = "Background sync status" },
        NotificationChannel(
            CHANNEL_GENERAL,
            "General",
            NotificationManager.IMPORTANCE_DEFAULT,
          )
          .apply { description = "General notifications" },
      )
    )
  }

  public companion object {
    public const val CHANNEL_REMINDERS: String = "reminders"
    public const val CHANNEL_SYNC: String = "sync_updates"
    public const val CHANNEL_GENERAL: String = "general"
  }
}
