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
class AccountDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: AccountDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.accountDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    val account = testAccount()
    dao.upsert(account)
    dao.getById(account.tenantId) shouldBe account
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    dao.upsert(testAccount())
    val updated = testAccount(displayName = "Updated")
    dao.upsert(updated)
    dao.getById("tenant-1")?.displayName shouldBe "Updated"
  }

  @Test
  fun `observeAll emits non-deleted accounts`() = runTest {
    dao.upsert(testAccount(tenantId = "t1"))
    dao.upsert(testAccount(tenantId = "t2", deletedAt = 999L))
    dao.observeAll().test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].tenantId shouldBe "t1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeById filters soft-deleted`() = runTest {
    dao.upsert(testAccount(deletedAt = 999L))
    dao.observeById("tenant-1").test {
      awaitItem().shouldBeNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete marks entity as deleted`() = runTest {
    dao.upsert(testAccount())
    dao.softDelete("tenant-1", deletedAt = 999L, updatedAt = 1000L)
    dao.observeAll().test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("tenant-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes entity`() = runTest {
    dao.upsert(testAccount())
    dao.hardDelete("tenant-1")
    dao.getById("tenant-1").shouldBeNull()
  }

  @Test
  fun `upsertAll inserts multiple entities`() = runTest {
    dao.upsertAll(listOf(testAccount(tenantId = "t1"), testAccount(tenantId = "t2")))
    dao.observeAll().test {
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeAll emits updates on mutation`() = runTest {
    dao.observeAll().test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testAccount())
      awaitItem() shouldHaveSize 1
      cancelAndIgnoreRemainingEvents()
    }
  }
}
