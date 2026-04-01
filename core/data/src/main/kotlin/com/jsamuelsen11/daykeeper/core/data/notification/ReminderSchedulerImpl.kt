package com.jsamuelsen11.daykeeper.core.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.jsamuelsen11.daykeeper.core.data.repository.EventReminderRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import com.jsamuelsen11.daykeeper.core.model.task.Task

private const val MILLIS_PER_MINUTE = 60_000L

/** Schedules exact alarms via [AlarmManager] for event and task reminders. */
public class ReminderSchedulerImpl(
  private val context: Context,
  private val eventReminderRepository: EventReminderRepository,
  private val eventRepository: EventRepository,
  private val taskRepository: TaskRepository,
) : ReminderScheduler {

  private val alarmManager: AlarmManager =
    context.getSystemService(AlarmManager::class.java)

  public override fun scheduleEventReminder(event: Event, reminder: EventReminder) {
    val startAt = event.startAt ?: return
    val triggerAt = startAt - (reminder.minutesBefore * MILLIS_PER_MINUTE)
    if (triggerAt <= System.currentTimeMillis()) return

    val intent = createReminderIntent(reminder.reminderId, ReminderType.EVENT)
    val pendingIntent =
      PendingIntent.getBroadcast(
        context,
        reminder.reminderId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
  }

  public override fun cancelEventReminder(reminderId: String) {
    val intent = createReminderIntent(reminderId, ReminderType.EVENT)
    val pendingIntent =
      PendingIntent.getBroadcast(
        context,
        reminderId.hashCode(),
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
      )
    pendingIntent?.let { alarmManager.cancel(it) }
  }

  public override fun scheduleTaskReminder(task: Task) {
    val dueAt = task.dueAt ?: return
    val minutesBefore = task.reminderMinutesBefore ?: return
    val triggerAt = dueAt - (minutesBefore * MILLIS_PER_MINUTE)
    if (triggerAt <= System.currentTimeMillis()) return

    val intent = createReminderIntent(task.taskId, ReminderType.TASK)
    val pendingIntent =
      PendingIntent.getBroadcast(
        context,
        task.taskId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
  }

  public override fun cancelTaskReminder(taskId: String) {
    val intent = createReminderIntent(taskId, ReminderType.TASK)
    val pendingIntent =
      PendingIntent.getBroadcast(
        context,
        taskId.hashCode(),
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
      )
    pendingIntent?.let { alarmManager.cancel(it) }
  }

  public override suspend fun rescheduleAllReminders() {
    val reminders = eventReminderRepository.getAllActive()
    for (reminder in reminders) {
      val event = eventRepository.getById(reminder.eventId) ?: continue
      scheduleEventReminder(event, reminder)
    }

    val tasks = taskRepository.getTasksWithReminders()
    for (task in tasks) {
      scheduleTaskReminder(task)
    }
  }

  private fun createReminderIntent(entityId: String, reminderType: ReminderType): Intent =
    Intent(ACTION_REMINDER_FIRED).apply {
      setPackage(context.packageName)
      putExtra(EXTRA_ENTITY_ID, entityId)
      putExtra(EXTRA_REMINDER_TYPE, reminderType.name)
    }

  public companion object {
    public const val ACTION_REMINDER_FIRED: String =
      "com.jsamuelsen11.daykeeper.action.REMINDER_FIRED"
    public const val EXTRA_ENTITY_ID: String = "entity_id"
    public const val EXTRA_REMINDER_TYPE: String = "reminder_type"
  }
}
