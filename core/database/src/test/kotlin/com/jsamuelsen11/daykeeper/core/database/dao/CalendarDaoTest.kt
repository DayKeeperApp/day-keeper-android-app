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
class CalendarDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: CalendarDao
  private lateinit var accountDao: AccountDao
  private lateinit var spaceDao: SpaceDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.calendarDao()
    accountDao = db.accountDao()
    spaceDao = db.spaceDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  private suspend fun insertParents(tenantId: String = "tenant-1", spaceId: String = "space-1") {
    accountDao.upsert(testAccount(tenantId = tenantId))
    spaceDao.upsert(testSpace(spaceId = spaceId, tenantId = tenantId))
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    insertParents()
    val calendar = testCalendar()
    dao.upsert(calendar)
    dao.getById(calendar.calendarId) shouldBe calendar
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    insertParents()
    dao.upsert(testCalendar())
    val updated = testCalendar(name = "Updated Calendar")
    dao.upsert(updated)
    dao.getById("cal-1")?.name shouldBe "Updated Calendar"
  }

  @Test
  fun `observeById emits entity and filters soft-deleted`() = runTest {
    insertParents()
    val calendar = testCalendar()
    dao.upsert(calendar)
    dao.observeById(calendar.calendarId).test {
      awaitItem() shouldBe calendar
      dao.softDelete(calendar.calendarId, deletedAt = 999L, updatedAt = 3_000L)
      awaitItem().shouldBeNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpace emits only non-deleted calendars for space`() = runTest {
    insertParents()
    dao.upsert(testCalendar(calendarId = "cal-1", name = "Active"))
    dao.upsert(testCalendar(calendarId = "cal-2", name = "Deleted", deletedAt = 999L))
    dao.observeBySpace("space-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].calendarId shouldBe "cal-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpace does not return calendars from other spaces`() = runTest {
    accountDao.upsert(testAccount(tenantId = "tenant-1"))
    spaceDao.upsert(testSpace(spaceId = "space-1", tenantId = "tenant-1"))
    spaceDao.upsert(testSpace(spaceId = "space-2", tenantId = "tenant-1"))
    dao.upsert(testCalendar(calendarId = "cal-1", spaceId = "space-1"))
    dao.upsert(testCalendar(calendarId = "cal-2", spaceId = "space-2"))
    dao.observeBySpace("space-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].calendarId shouldBe "cal-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete marks entity as deleted and getById still returns it`() = runTest {
    insertParents()
    dao.upsert(testCalendar())
    dao.softDelete("cal-1", deletedAt = 999L, updatedAt = 3_000L)
    dao.observeBySpace("space-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("cal-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes entity entirely`() = runTest {
    insertParents()
    dao.upsert(testCalendar())
    dao.hardDelete("cal-1")
    dao.getById("cal-1").shouldBeNull()
  }

  @Test
  fun `upsertAll inserts multiple calendars`() = runTest {
    insertParents()
    dao.upsertAll(
      listOf(
        testCalendar(calendarId = "cal-1", name = "Work"),
        testCalendar(calendarId = "cal-2", name = "Personal"),
      )
    )
    dao.observeBySpace("space-1").test {
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpace emits updates on mutation`() = runTest {
    insertParents()
    dao.observeBySpace("space-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testCalendar())
      awaitItem() shouldHaveSize 1
      cancelAndIgnoreRemainingEvents()
    }
  }
}
