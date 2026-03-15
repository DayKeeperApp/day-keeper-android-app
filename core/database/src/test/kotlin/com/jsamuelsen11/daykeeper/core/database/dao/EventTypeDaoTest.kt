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
class EventTypeDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: EventTypeDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.eventTypeDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    val eventType = testEventType()
    dao.upsert(eventType)
    dao.getById(eventType.eventTypeId) shouldBe eventType
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    dao.upsert(testEventType())
    val updated = testEventType(name = "Conference")
    dao.upsert(updated)
    dao.getById("etype-1")?.name shouldBe "Conference"
  }

  @Test
  fun `observeAll emits all event types`() = runTest {
    dao.upsert(testEventType(eventTypeId = "etype-1", name = "Meeting"))
    dao.upsert(testEventType(eventTypeId = "etype-2", name = "Holiday"))
    dao.observeAll().test {
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeAll emits empty list when table is empty`() = runTest {
    dao.observeAll().test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `hardDelete removes entity`() = runTest {
    dao.upsert(testEventType())
    dao.hardDelete("etype-1")
    dao.getById("etype-1").shouldBeNull()
  }

  @Test
  fun `upsertAll inserts multiple event types`() = runTest {
    dao.upsertAll(
      listOf(
        testEventType(eventTypeId = "etype-1", name = "Meeting"),
        testEventType(eventTypeId = "etype-2", name = "Holiday"),
        testEventType(eventTypeId = "etype-3", name = "Birthday"),
      )
    )
    dao.observeAll().test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeAll emits updates on mutation`() = runTest {
    dao.observeAll().test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testEventType())
      awaitItem() shouldHaveSize 1
      dao.hardDelete("etype-1")
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
