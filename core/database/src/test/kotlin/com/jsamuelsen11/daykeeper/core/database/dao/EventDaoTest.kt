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
class EventDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: EventDao
  private lateinit var accountDao: AccountDao
  private lateinit var spaceDao: SpaceDao
  private lateinit var calendarDao: CalendarDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.eventDao()
    accountDao = db.accountDao()
    spaceDao = db.spaceDao()
    calendarDao = db.calendarDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  private suspend fun insertParents(
    tenantId: String = "tenant-1",
    spaceId: String = "space-1",
    calendarId: String = "cal-1",
  ) {
    accountDao.upsert(testAccount(tenantId = tenantId))
    spaceDao.upsert(testSpace(spaceId = spaceId, tenantId = tenantId))
    calendarDao.upsert(
      testCalendar(calendarId = calendarId, spaceId = spaceId, tenantId = tenantId)
    )
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    insertParents()
    val event = testEvent()
    dao.upsert(event)
    dao.getById(event.eventId) shouldBe event
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    insertParents()
    dao.upsert(testEvent())
    val updated = testEvent(title = "Updated Title")
    dao.upsert(updated)
    dao.getById("event-1")?.title shouldBe "Updated Title"
  }

  @Test
  fun `observeById emits entity and filters soft-deleted`() = runTest {
    insertParents()
    val event = testEvent()
    dao.upsert(event)
    dao.observeById(event.eventId).test {
      awaitItem() shouldBe event
      dao.softDelete(event.eventId, deletedAt = 999L, updatedAt = 3_000L)
      awaitItem().shouldBeNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByCalendar emits only non-deleted events for calendar`() = runTest {
    insertParents()
    dao.upsert(testEvent(eventId = "event-1", title = "Active"))
    dao.upsert(testEvent(eventId = "event-2", title = "Deleted", deletedAt = 999L))
    dao.observeByCalendar("cal-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].eventId shouldBe "event-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByCalendarAndRange returns events whose start_at falls within range`() = runTest {
    insertParents()
    // inside range: 100_000 to 200_000
    dao.upsert(testEvent(eventId = "event-in-1", startAt = 100_000L))
    dao.upsert(testEvent(eventId = "event-in-2", startAt = 150_000L))
    dao.upsert(testEvent(eventId = "event-in-3", startAt = 200_000L))
    // outside range
    dao.upsert(testEvent(eventId = "event-before", startAt = 99_999L))
    dao.upsert(testEvent(eventId = "event-after", startAt = 200_001L))
    dao.observeByCalendarAndRange("cal-1", startMillis = 100_000L, endMillis = 200_000L).test {
      val result = awaitItem()
      result shouldHaveSize 3
      result.map { it.eventId }.toSet() shouldBe setOf("event-in-1", "event-in-2", "event-in-3")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByCalendarAndRange excludes soft-deleted events`() = runTest {
    insertParents()
    dao.upsert(testEvent(eventId = "event-active", startAt = 150_000L))
    dao.upsert(testEvent(eventId = "event-deleted", startAt = 150_000L, deletedAt = 999L))
    dao.observeByCalendarAndRange("cal-1", startMillis = 100_000L, endMillis = 200_000L).test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].eventId shouldBe "event-active"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeAllDayByCalendarAndRange returns all-day events within date range`() = runTest {
    insertParents()
    dao.upsert(
      testEvent(
        eventId = "allday-in",
        isAllDay = true,
        startAt = null,
        endAt = null,
        startDate = "2026-03-10",
        endDate = "2026-03-10",
      )
    )
    dao.upsert(
      testEvent(
        eventId = "allday-out",
        isAllDay = true,
        startAt = null,
        endAt = null,
        startDate = "2026-04-01",
        endDate = "2026-04-01",
      )
    )
    dao
      .observeAllDayByCalendarAndRange("cal-1", startDate = "2026-03-01", endDate = "2026-03-31")
      .test {
        val result = awaitItem()
        result shouldHaveSize 1
        result[0].eventId shouldBe "allday-in"
        cancelAndIgnoreRemainingEvents()
      }
  }

  @Test
  fun `softDelete marks entity as deleted and getById still returns it`() = runTest {
    insertParents()
    dao.upsert(testEvent())
    dao.softDelete("event-1", deletedAt = 999L, updatedAt = 3_000L)
    dao.observeByCalendar("cal-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("event-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes entity entirely`() = runTest {
    insertParents()
    dao.upsert(testEvent())
    dao.hardDelete("event-1")
    dao.getById("event-1").shouldBeNull()
  }

  @Test
  fun `upsertAll inserts multiple events`() = runTest {
    insertParents()
    dao.upsertAll(
      listOf(
        testEvent(eventId = "event-1", title = "First"),
        testEvent(eventId = "event-2", title = "Second"),
      )
    )
    dao.observeByCalendar("cal-1").test {
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByCalendar emits updates on mutation`() = runTest {
    insertParents()
    dao.observeByCalendar("cal-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testEvent())
      awaitItem() shouldHaveSize 1
      cancelAndIgnoreRemainingEvents()
    }
  }
}
