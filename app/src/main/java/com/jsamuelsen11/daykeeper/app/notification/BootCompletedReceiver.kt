package com.jsamuelsen11.daykeeper.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jsamuelsen11.daykeeper.core.data.notification.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Reschedules all reminders after device reboot. */
class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {

  private val reminderScheduler: ReminderScheduler by inject()

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
      try {
        reminderScheduler.rescheduleAllReminders()
      } finally {
        pendingResult.finish()
      }
    }
  }
}
