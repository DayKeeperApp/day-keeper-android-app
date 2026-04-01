package com.jsamuelsen11.daykeeper.core.data.notification

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.S])
class NotificationDisplayManagerTest {

  private lateinit var context: Context
  private lateinit var displayManager: NotificationDisplayManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    NotificationChannelManager(context).createChannels()
    displayManager = NotificationDisplayManager(context)
  }

  @Test
  fun `showEventReminder posts notification with event title`() {
    val event = testEvent()
    val reminder = testReminder()

    displayManager.showEventReminder(event, reminder)

    val shadowManager =
      Shadows.shadowOf(context.getSystemService(android.app.NotificationManager::class.java))
    val notifications = shadowManager.allNotifications
    notifications.size shouldBe 1
  }

  @Test
  fun `showTaskReminder posts notification with task title`() {
    val task = testTask()

    displayManager.showTaskReminder(task)

    val shadowManager =
      Shadows.shadowOf(context.getSystemService(android.app.NotificationManager::class.java))
    val notifications = shadowManager.allNotifications
    notifications.size shouldBe 1
  }

  @Test
  fun `showSyncNotification posts notification on sync channel`() {
    displayManager.showSyncNotification("Sync complete")

    val shadowManager =
      Shadows.shadowOf(context.getSystemService(android.app.NotificationManager::class.java))
    val notifications = shadowManager.allNotifications
    notifications.size shouldBe 1
  }

  @Test
  fun `showGeneralNotification posts notification`() {
    displayManager.showGeneralNotification("Title", "Body", GENERAL_NOTIFICATION_ID)

    val shadowManager =
      Shadows.shadowOf(context.getSystemService(android.app.NotificationManager::class.java))
    val notifications = shadowManager.allNotifications
    notifications.size shouldBe 1
  }

  @Test
  fun `cancel removes notification`() {
    displayManager.showGeneralNotification("Title", "Body", GENERAL_NOTIFICATION_ID)
    displayManager.cancel(GENERAL_NOTIFICATION_ID)

    val shadowManager =
      Shadows.shadowOf(context.getSystemService(android.app.NotificationManager::class.java))
    shadowManager.allNotifications.size shouldBe 0
  }

  @Test
  fun `showEventReminder notification has snooze action`() {
    displayManager.showEventReminder(testEvent(), testReminder())

    val shadowManager =
      Shadows.shadowOf(context.getSystemService(android.app.NotificationManager::class.java))
    val notification = shadowManager.allNotifications.first()
    notification.actions.shouldNotBeNull()
    notification.actions.size shouldBe EXPECTED_EVENT_ACTIONS
  }

  @Test
  fun `showTaskReminder notification has mark done and snooze actions`() {
    displayManager.showTaskReminder(testTask())

    val shadowManager =
      Shadows.shadowOf(context.getSystemService(android.app.NotificationManager::class.java))
    val notification = shadowManager.allNotifications.first()
    notification.actions.shouldNotBeNull()
    notification.actions.size shouldBe EXPECTED_TASK_ACTIONS
  }

  private fun testEvent() =
    Event(
      eventId = TEST_EVENT_ID,
      calendarId = "cal-1",
      spaceId = "space-1",
      tenantId = "tenant-1",
      title = "Test Event",
      description = null,
      startAt = FUTURE_TIME,
      endAt = FUTURE_TIME + HOUR_MILLIS,
      startDate = null,
      endDate = null,
      isAllDay = false,
      timezone = "UTC",
      eventTypeId = null,
      location = null,
      recurrenceRule = null,
      parentEventId = null,
      createdAt = NOW,
      updatedAt = NOW,
    )

  private fun testReminder() =
    EventReminder(
      reminderId = TEST_REMINDER_ID,
      eventId = TEST_EVENT_ID,
      minutesBefore = REMINDER_MINUTES,
      createdAt = NOW,
      updatedAt = NOW,
    )

  private fun testTask() =
    Task(
      taskId = TEST_TASK_ID,
      spaceId = "space-1",
      tenantId = "tenant-1",
      title = "Test Task",
      status = TaskStatus.TODO,
      priority = Priority.MEDIUM,
      dueAt = FUTURE_TIME,
      reminderMinutesBefore = REMINDER_MINUTES,
      createdAt = NOW,
      updatedAt = NOW,
    )

  companion object {
    private const val TEST_EVENT_ID = "event-123"
    private const val TEST_REMINDER_ID = "reminder-456"
    private const val TEST_TASK_ID = "task-789"
    private const val NOW = 1_000_000L
    private const val FUTURE_TIME = 9_999_999_999L
    private const val HOUR_MILLIS = 3_600_000L
    private const val REMINDER_MINUTES = 15
    private const val GENERAL_NOTIFICATION_ID = 9999
    private const val EXPECTED_EVENT_ACTIONS = 1
    private const val EXPECTED_TASK_ACTIONS = 2
  }
}
