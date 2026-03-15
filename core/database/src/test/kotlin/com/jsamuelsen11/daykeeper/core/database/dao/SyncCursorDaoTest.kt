package com.jsamuelsen11.daykeeper.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.database.DayKeeperDatabase
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
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
class SyncCursorDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: SyncCursorDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.syncCursorDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `upsert and getCursor returns entity`() = runTest {
    val cursor = testSyncCursor()
    dao.upsert(cursor)
    dao.getCursor() shouldBe cursor
  }

  @Test
  fun `getCursor returns null when table is empty`() = runTest { dao.getCursor().shouldBeNull() }

  @Test
  fun `upsert updates existing cursor`() = runTest {
    dao.upsert(testSyncCursor(lastCursor = 100L, lastSyncAt = 5_000L))
    dao.upsert(testSyncCursor(lastCursor = 200L, lastSyncAt = 6_000L))
    val cursor = dao.getCursor()
    cursor.shouldNotBeNull()
    cursor.lastCursor shouldBe 200L
    cursor.lastSyncAt shouldBe 6_000L
  }

  @Test
  fun `observeCursor emits null when table is empty`() = runTest {
    dao.observeCursor().test {
      awaitItem().shouldBeNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeCursor emits entity after upsert`() = runTest {
    val cursor = testSyncCursor()
    dao.upsert(cursor)
    dao.observeCursor().test {
      awaitItem() shouldBe cursor
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeCursor emits updated value on subsequent upsert`() = runTest {
    dao.observeCursor().test {
      awaitItem().shouldBeNull()
      dao.upsert(testSyncCursor(lastCursor = 100L))
      awaitItem()?.lastCursor shouldBe 100L
      dao.upsert(testSyncCursor(lastCursor = 250L))
      awaitItem()?.lastCursor shouldBe 250L
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `getCursor returns only the single most recent row`() = runTest {
    dao.upsert(testSyncCursor(id = "sync_cursor", lastCursor = 42L))
    val result = dao.getCursor()
    result.shouldNotBeNull()
    result.id shouldBe "sync_cursor"
    result.lastCursor shouldBe 42L
  }
}
