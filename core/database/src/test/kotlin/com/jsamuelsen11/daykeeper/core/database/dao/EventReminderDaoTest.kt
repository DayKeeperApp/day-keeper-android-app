package com.jsamuelsen11.daykeeper.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.database.DayKeeperDatabase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class EventReminderDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: EventReminderDao
  private lateinit var accountDao: AccountDao
  private lateinit var spaceDao: SpaceDao
  private lateinit var calendarDao: CalendarDao
  private lateinit var eventDao: EventDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.eventReminderDao()
    accountDao = db.accountDao()
    spaceDao = db.spaceDao()
    calendarDao = db.calendarDao()
    eventDao = db.eventDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  private suspend fun insertParents(
    tenantId: String = "tenant-1",
    spaceId: String = "space-1",
    calendarId: String = "cal-1",
    eventId: String = "event-1",
  ) {
    accountDao.upsert(testAccount(tenantId = tenantId))
    spaceDao.upsert(testSpace(spaceId = spaceId, tenantId = tenantId))
    calendarDao.upsert(
      testCalendar(calendarId = calendarId, spaceId = spaceId, tenantId = tenantId)
    )
    eventDao.upsert(
      testEvent(eventId = eventId, calendarId = calendarId, spaceId = spaceId, tenantId = tenantId)
    )
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    insertParents()
    val reminder = testEventReminder()
    dao.upsert(reminder)
    dao.getById(reminder.reminderId) shouldBe reminder
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    insertParents()
    dao.upsert(testEventReminder())
    val updated = testEventReminder(minutesBefore = 30)
    dao.upsert(updated)
    dao.getById("reminder-1")?.minutesBefore shouldBe 30
  }

  @Test
  fun `observeByEvent emits only non-deleted reminders for event`() = runTest {
    insertParents()
    dao.upsert(testEventReminder(reminderId = "reminder-1", minutesBefore = 15))
    dao.upsert(testEventReminder(reminderId = "reminder-2", minutesBefore = 30, deletedAt = 999L))
    dao.observeByEvent("event-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].reminderId shouldBe "reminder-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByEvent does not return reminders from other events`() = runTest {
    insertParents(eventId = "event-1")
    eventDao.upsert(
      testEvent(
        eventId = "event-2",
        calendarId = "cal-1",
        spaceId = "space-1",
        tenantId = "tenant-1",
      )
    )
    dao.upsert(testEventReminder(reminderId = "reminder-1", eventId = "event-1"))
    dao.upsert(testEventReminder(reminderId = "reminder-2", eventId = "event-2"))
    dao.observeByEvent("event-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].reminderId shouldBe "reminder-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete marks entity as deleted and getById still returns it`() = runTest {
    insertParents()
    dao.upsert(testEventReminder())
    dao.softDelete("reminder-1", deletedAt = 999L, updatedAt = 3_000L)
    dao.observeByEvent("event-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("reminder-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes entity entirely`() = runTest {
    insertParents()
    dao.upsert(testEventReminder())
    dao.hardDelete("reminder-1")
    dao.getById("reminder-1").shouldBeNull()
  }

  @Test
  fun `upsertAll inserts multiple reminders`() = runTest {
    insertParents()
    dao.upsertAll(
      listOf(
        testEventReminder(reminderId = "reminder-1", minutesBefore = 5),
        testEventReminder(reminderId = "reminder-2", minutesBefore = 15),
        testEventReminder(reminderId = "reminder-3", minutesBefore = 60),
      )
    )
    dao.observeByEvent("event-1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByEvent emits updates on mutation`() = runTest {
    insertParents()
    dao.observeByEvent("event-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testEventReminder())
      awaitItem() shouldHaveSize 1
      cancelAndIgnoreRemainingEvents()
    }
  }
}
