package com.jsamuelsen11.daykeeper.core.data.notification

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.jsamuelsen11.daykeeper.core.data.repository.EventReminderRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.R])
class ReminderSchedulerImplTest {

  private lateinit var context: Context
  private lateinit var eventReminderRepository: EventReminderRepository
  private lateinit var eventRepository: EventRepository
  private lateinit var taskRepository: TaskRepository
  private lateinit var scheduler: ReminderSchedulerImpl

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    eventReminderRepository = mockk(relaxed = true)
    eventRepository = mockk(relaxed = true)
    taskRepository = mockk(relaxed = true)
    scheduler =
      ReminderSchedulerImpl(context, eventReminderRepository, eventRepository, taskRepository)
  }

  @Test
  fun `scheduleEventReminder sets alarm at correct trigger time`() {
    val event = testEvent(startAt = FUTURE_TIME)
    val reminder = testReminder(minutesBefore = REMINDER_MINUTES)
    val expectedTrigger = FUTURE_TIME - (REMINDER_MINUTES * MILLIS_PER_MINUTE)

    scheduler.scheduleEventReminder(event, reminder)

    val shadowAlarmManager = Shadows.shadowOf(context.getSystemService(AlarmManager::class.java))
    val nextAlarm = shadowAlarmManager.nextScheduledAlarm
    nextAlarm.shouldNotBeNull()
    nextAlarm.triggerAtTime shouldBe expectedTrigger
  }

  @Test
  fun `scheduleEventReminder skips past events`() {
    val event = testEvent(startAt = PAST_TIME)
    val reminder = testReminder(minutesBefore = REMINDER_MINUTES)

    scheduler.scheduleEventReminder(event, reminder)

    val shadowAlarmManager = Shadows.shadowOf(context.getSystemService(AlarmManager::class.java))
    shadowAlarmManager.nextScheduledAlarm shouldBe null
  }

  @Test
  fun `scheduleEventReminder skips all-day events with null startAt`() {
    val event = testEvent(startAt = null)
    val reminder = testReminder(minutesBefore = REMINDER_MINUTES)

    scheduler.scheduleEventReminder(event, reminder)

    val shadowAlarmManager = Shadows.shadowOf(context.getSystemService(AlarmManager::class.java))
    shadowAlarmManager.nextScheduledAlarm shouldBe null
  }

  @Test
  fun `scheduleTaskReminder sets alarm at correct trigger time`() {
    val task = testTask(dueAt = FUTURE_TIME, reminderMinutesBefore = REMINDER_MINUTES)

    scheduler.scheduleTaskReminder(task)

    val shadowAlarmManager = Shadows.shadowOf(context.getSystemService(AlarmManager::class.java))
    val nextAlarm = shadowAlarmManager.nextScheduledAlarm
    nextAlarm.shouldNotBeNull()
    nextAlarm.triggerAtTime shouldBe FUTURE_TIME - (REMINDER_MINUTES * MILLIS_PER_MINUTE)
  }

  @Test
  fun `scheduleTaskReminder skips task without dueAt`() {
    val task = testTask(dueAt = null, reminderMinutesBefore = REMINDER_MINUTES)

    scheduler.scheduleTaskReminder(task)

    val shadowAlarmManager = Shadows.shadowOf(context.getSystemService(AlarmManager::class.java))
    shadowAlarmManager.nextScheduledAlarm shouldBe null
  }

  @Test
  fun `scheduleTaskReminder skips task without reminder`() {
    val task = testTask(dueAt = FUTURE_TIME, reminderMinutesBefore = null)

    scheduler.scheduleTaskReminder(task)

    val shadowAlarmManager = Shadows.shadowOf(context.getSystemService(AlarmManager::class.java))
    shadowAlarmManager.nextScheduledAlarm shouldBe null
  }

  @Test
  fun `rescheduleAllReminders schedules all active reminders`() = runTest {
    val event = testEvent(startAt = FUTURE_TIME)
    val reminder = testReminder(minutesBefore = REMINDER_MINUTES)
    val task = testTask(dueAt = FUTURE_TIME, reminderMinutesBefore = REMINDER_MINUTES)

    coEvery { eventReminderRepository.getAllActive() } returns listOf(reminder)
    coEvery { eventRepository.getById(TEST_EVENT_ID) } returns event
    coEvery { taskRepository.getTasksWithReminders() } returns listOf(task)

    scheduler.rescheduleAllReminders()

    val shadowAlarmManager = Shadows.shadowOf(context.getSystemService(AlarmManager::class.java))
    shadowAlarmManager.scheduledAlarms.size shouldBe EXPECTED_ALARM_COUNT
  }

  @Test
  fun `cancelEventReminder removes scheduled alarm`() {
    val event = testEvent(startAt = FUTURE_TIME)
    val reminder = testReminder(minutesBefore = REMINDER_MINUTES)
    scheduler.scheduleEventReminder(event, reminder)

    scheduler.cancelEventReminder(TEST_REMINDER_ID)

    val shadowAlarmManager = Shadows.shadowOf(context.getSystemService(AlarmManager::class.java))
    shadowAlarmManager.scheduledAlarms.size shouldBe 0
  }

  private fun testEvent(startAt: Long?) =
    Event(
      eventId = TEST_EVENT_ID,
      calendarId = TEST_CALENDAR_ID,
      spaceId = TEST_SPACE_ID,
      tenantId = TEST_TENANT_ID,
      title = "Test Event",
      description = null,
      startAt = startAt,
      endAt = startAt?.let { it + HOUR_MILLIS },
      startDate = null,
      endDate = null,
      isAllDay = startAt == null,
      timezone = "UTC",
      eventTypeId = null,
      location = null,
      recurrenceRule = null,
      parentEventId = null,
      createdAt = NOW,
      updatedAt = NOW,
    )

  private fun testReminder(minutesBefore: Int) =
    EventReminder(
      reminderId = TEST_REMINDER_ID,
      eventId = TEST_EVENT_ID,
      minutesBefore = minutesBefore,
      createdAt = NOW,
      updatedAt = NOW,
    )

  private fun testTask(dueAt: Long?, reminderMinutesBefore: Int?) =
    Task(
      taskId = TEST_TASK_ID,
      spaceId = TEST_SPACE_ID,
      tenantId = TEST_TENANT_ID,
      title = "Test Task",
      status = TaskStatus.TODO,
      priority = Priority.MEDIUM,
      dueAt = dueAt,
      reminderMinutesBefore = reminderMinutesBefore,
      createdAt = NOW,
      updatedAt = NOW,
    )

  companion object {
    private const val TEST_EVENT_ID = "event-123"
    private const val TEST_CALENDAR_ID = "cal-456"
    private const val TEST_REMINDER_ID = "reminder-789"
    private const val TEST_TASK_ID = "task-abc"
    private const val TEST_SPACE_ID = "space-def"
    private const val TEST_TENANT_ID = "tenant-ghi"
    private const val NOW = 1_000_000L
    private const val MILLIS_PER_MINUTE = 60_000L
    private const val HOUR_MILLIS = 3_600_000L
    private const val REMINDER_MINUTES = 15
    private const val FUTURE_TIME = Long.MAX_VALUE / 2
    private const val PAST_TIME = 1L
    private const val EXPECTED_ALARM_COUNT = 2
  }
}
