package com.jsamuelsen11.daykeeper.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.database.DayKeeperDatabase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
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
class SpaceDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: SpaceDao
  private lateinit var accountDao: AccountDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.spaceDao()
    accountDao = db.accountDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    accountDao.upsert(testAccount())
    val space = testSpace()
    dao.upsert(space)
    dao.getById(space.spaceId) shouldBe space
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    accountDao.upsert(testAccount())
    dao.upsert(testSpace())
    val updated = testSpace(name = "Updated Space")
    dao.upsert(updated)
    dao.getById("space-1")?.name shouldBe "Updated Space"
  }

  @Test
  fun `upsertAll inserts multiple entities`() = runTest {
    accountDao.upsert(testAccount())
    dao.upsertAll(
      listOf(testSpace(spaceId = "s1"), testSpace(spaceId = "s2"), testSpace(spaceId = "s3"))
    )
    dao.observeByTenant("tenant-1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeById emits entity when present and not deleted`() = runTest {
    accountDao.upsert(testAccount())
    dao.upsert(testSpace())
    dao.observeById("space-1").test {
      awaitItem().shouldNotBeNull().spaceId shouldBe "space-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeById filters soft-deleted entity`() = runTest {
    accountDao.upsert(testAccount())
    dao.upsert(testSpace(deletedAt = 999L))
    dao.observeById("space-1").test {
      awaitItem().shouldBeNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByTenant returns only non-deleted spaces for tenant`() = runTest {
    accountDao.upsert(testAccount(tenantId = "t1"))
    accountDao.upsert(testAccount(tenantId = "t2"))
    dao.upsert(testSpace(spaceId = "s1", tenantId = "t1"))
    dao.upsert(testSpace(spaceId = "s2", tenantId = "t1", deletedAt = 999L))
    dao.upsert(testSpace(spaceId = "s3", tenantId = "t2"))
    dao.observeByTenant("t1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].spaceId shouldBe "s1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete marks entity as deleted and hides it from observe`() = runTest {
    accountDao.upsert(testAccount())
    dao.upsert(testSpace())
    dao.softDelete(spaceId = "space-1", deletedAt = 999L, updatedAt = 1_000L)
    dao.observeByTenant("tenant-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("space-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes entity`() = runTest {
    accountDao.upsert(testAccount())
    dao.upsert(testSpace())
    dao.hardDelete("space-1")
    dao.getById("space-1").shouldBeNull()
  }

  @Test
  fun `observeByTenant emits updates on mutation`() = runTest {
    accountDao.upsert(testAccount())
    dao.observeByTenant("tenant-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testSpace(spaceId = "s1"))
      awaitItem() shouldHaveSize 1
      dao.upsert(testSpace(spaceId = "s2"))
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }
}
