package com.jsamuelsen11.daykeeper.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.jsamuelsen11.daykeeper.core.data.notification.NotificationDisplayManager
import com.jsamuelsen11.daykeeper.core.data.notification.ReminderSchedulerImpl
import com.jsamuelsen11.daykeeper.core.data.notification.ReminderType
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val MILLIS_PER_MINUTE = 60_000L

/** Handles notification action buttons: Mark Done and Snooze. */
class NotificationActionReceiver : BroadcastReceiver(), KoinComponent {

  private val taskRepository: TaskRepository by inject()

  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      NotificationDisplayManager.ACTION_MARK_DONE -> handleMarkDone(context, intent)
      NotificationDisplayManager.ACTION_SNOOZE -> handleSnooze(context, intent)
    }
  }

  private fun handleMarkDone(context: Context, intent: Intent) {
    val taskId = intent.getStringExtra(NotificationDisplayManager.EXTRA_TASK_ID) ?: return
    val notificationId = intent.getIntExtra(NotificationDisplayManager.EXTRA_NOTIFICATION_ID, -1)

    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val task = taskRepository.getById(taskId) ?: return@launch
        taskRepository.upsert(
          task.copy(status = TaskStatus.DONE, updatedAt = System.currentTimeMillis())
        )
        if (notificationId != -1) {
          NotificationManagerCompat.from(context).cancel(notificationId)
        }
      } finally {
        pendingResult.finish()
      }
    }
  }

  private fun handleSnooze(context: Context, intent: Intent) {
    val entityId = intent.getStringExtra(NotificationDisplayManager.EXTRA_ENTITY_ID)
    val typeName = intent.getStringExtra(NotificationDisplayManager.EXTRA_REMINDER_TYPE)
    val snoozeMinutes = intent.getIntExtra(NotificationDisplayManager.EXTRA_SNOOZE_MINUTES, 0)
    if (entityId == null || typeName == null || snoozeMinutes <= 0) return

    val triggerAt = System.currentTimeMillis() + (snoozeMinutes * MILLIS_PER_MINUTE)
    val reminderType = ReminderType.valueOf(typeName)

    val alarmIntent =
      Intent(ReminderSchedulerImpl.ACTION_REMINDER_FIRED).apply {
        setPackage(context.packageName)
        putExtra(ReminderSchedulerImpl.EXTRA_ENTITY_ID, entityId)
        putExtra(ReminderSchedulerImpl.EXTRA_REMINDER_TYPE, reminderType.name)
      }

    val pendingIntent =
      PendingIntent.getBroadcast(
        context,
        "snooze_$entityId".hashCode(),
        alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val alarmManager = context.getSystemService(AlarmManager::class.java)
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
  }
}
