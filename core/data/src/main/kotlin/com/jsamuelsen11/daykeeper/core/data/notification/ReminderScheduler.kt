package com.jsamuelsen11.daykeeper.core.data.notification

import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import com.jsamuelsen11.daykeeper.core.model.task.Task

/** Schedules and cancels local alarm-based reminders for events and tasks. */
public interface ReminderScheduler {
  public fun scheduleEventReminder(event: Event, reminder: EventReminder)

  public fun cancelEventReminder(reminderId: String)

  public fun scheduleTaskReminder(task: Task)

  public fun cancelTaskReminder(taskId: String)

  /** Reschedules all active reminders, e.g. after device reboot. */
  public suspend fun rescheduleAllReminders()
}
