package com.jsamuelsen11.daykeeper.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jsamuelsen11.daykeeper.core.data.notification.NotificationDisplayManager
import com.jsamuelsen11.daykeeper.core.data.notification.ReminderSchedulerImpl
import com.jsamuelsen11.daykeeper.core.data.notification.ReminderType
import com.jsamuelsen11.daykeeper.core.data.repository.EventReminderRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Receives fired alarm intents and displays the corresponding notification. */
class ReminderBroadcastReceiver : BroadcastReceiver(), KoinComponent {

  private val eventReminderRepository: EventReminderRepository by inject()
  private val eventRepository: EventRepository by inject()
  private val taskRepository: TaskRepository by inject()
  private val displayManager: NotificationDisplayManager by inject()

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != ReminderSchedulerImpl.ACTION_REMINDER_FIRED) return

    val entityId = intent.getStringExtra(ReminderSchedulerImpl.EXTRA_ENTITY_ID) ?: return
    val typeName = intent.getStringExtra(ReminderSchedulerImpl.EXTRA_REMINDER_TYPE) ?: return
    val reminderType = ReminderType.valueOf(typeName)

    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
      try {
        when (reminderType) {
          ReminderType.EVENT -> handleEventReminder(entityId)
          ReminderType.TASK -> handleTaskReminder(entityId)
        }
      } finally {
        pendingResult.finish()
      }
    }
  }

  private suspend fun handleEventReminder(reminderId: String) {
    val reminder = eventReminderRepository.getById(reminderId) ?: return
    val event = eventRepository.getById(reminder.eventId) ?: return
    displayManager.showEventReminder(event, reminder)
  }

  private suspend fun handleTaskReminder(taskId: String) {
    val task = taskRepository.getById(taskId) ?: return
    displayManager.showTaskReminder(task)
  }
}
