package com.jsamuelsen11.daykeeper.core.data.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import com.jsamuelsen11.daykeeper.core.model.task.Task

private const val EVENT_NOTIFICATION_ID_OFFSET = 100_000
private const val TASK_NOTIFICATION_ID_OFFSET = 200_000
private const val SNOOZE_MINUTES = 10

/** Builds and displays notifications for reminders, sync, and general messages. */
public class NotificationDisplayManager(private val context: Context) {

  private val notificationManager = NotificationManagerCompat.from(context)

  public fun showEventReminder(event: Event, reminder: EventReminder) {
    val notificationId = EVENT_NOTIFICATION_ID_OFFSET + reminder.reminderId.hashCode()
    val contentIntent = createDeepLinkIntent(DeepLinkConstants.TYPE_EVENT, event.eventId)
    val snoozeIntent = createSnoozeIntent(reminder.reminderId, ReminderType.EVENT)

    val notification =
      NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_REMINDERS)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle(event.title)
        .setContentText("Reminder: ${reminder.minutesBefore} minutes before")
        .setContentIntent(contentIntent)
        .addAction(
          android.R.drawable.ic_popup_reminder,
          "Snooze ${SNOOZE_MINUTES}min",
          snoozeIntent,
        )
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    notificationManager.notify(notificationId, notification)
  }

  public fun showTaskReminder(task: Task) {
    val notificationId = TASK_NOTIFICATION_ID_OFFSET + task.taskId.hashCode()
    val contentIntent = createDeepLinkIntent(DeepLinkConstants.TYPE_TASK, task.taskId)
    val markDoneIntent = createMarkDoneIntent(task.taskId, notificationId)
    val snoozeIntent = createSnoozeIntent(task.taskId, ReminderType.TASK)

    val notification =
      NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_REMINDERS)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle(task.title)
        .setContentText("Task reminder")
        .setContentIntent(contentIntent)
        .addAction(android.R.drawable.ic_menu_send, "Mark Done", markDoneIntent)
        .addAction(
          android.R.drawable.ic_popup_reminder,
          "Snooze ${SNOOZE_MINUTES}min",
          snoozeIntent,
        )
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    notificationManager.notify(notificationId, notification)
  }

  public fun showSyncNotification(message: String) {
    val notification =
      NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_SYNC)
        .setSmallIcon(android.R.drawable.stat_notify_sync)
        .setContentTitle("Sync Update")
        .setContentText(message)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
  }

  public fun showGeneralNotification(title: String, body: String, notificationId: Int) {
    val notification =
      NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_GENERAL)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(body)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(notificationId, notification)
  }

  public fun cancel(notificationId: Int) {
    notificationManager.cancel(notificationId)
  }

  private fun createDeepLinkIntent(type: String, entityId: String): PendingIntent {
    val intent =
      context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        putExtra(DeepLinkConstants.EXTRA_DEEP_LINK_TYPE, type)
        putExtra(DeepLinkConstants.EXTRA_ENTITY_ID, entityId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
    return PendingIntent.getActivity(
      context,
      entityId.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private fun createMarkDoneIntent(taskId: String, notificationId: Int): PendingIntent {
    val intent =
      Intent(ACTION_MARK_DONE).apply {
        setPackage(context.packageName)
        putExtra(EXTRA_TASK_ID, taskId)
        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
      }
    return PendingIntent.getBroadcast(
      context,
      taskId.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private fun createSnoozeIntent(entityId: String, reminderType: ReminderType): PendingIntent {
    val intent =
      Intent(ACTION_SNOOZE).apply {
        setPackage(context.packageName)
        putExtra(EXTRA_ENTITY_ID, entityId)
        putExtra(EXTRA_REMINDER_TYPE, reminderType.name)
        putExtra(EXTRA_SNOOZE_MINUTES, SNOOZE_MINUTES)
      }
    return PendingIntent.getBroadcast(
      context,
      "snooze_$entityId".hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  public companion object {
    public const val ACTION_MARK_DONE: String =
      "com.jsamuelsen11.daykeeper.action.NOTIFICATION_MARK_DONE"
    public const val ACTION_SNOOZE: String =
      "com.jsamuelsen11.daykeeper.action.NOTIFICATION_SNOOZE"
    public const val EXTRA_TASK_ID: String = "task_id"
    public const val EXTRA_ENTITY_ID: String = "entity_id"
    public const val EXTRA_NOTIFICATION_ID: String = "notification_id"
    public const val EXTRA_REMINDER_TYPE: String = "reminder_type"
    public const val EXTRA_SNOOZE_MINUTES: String = "snooze_minutes"
    private const val SYNC_NOTIFICATION_ID = 1
  }
}

public enum class ReminderType {
  EVENT,
  TASK,
}
