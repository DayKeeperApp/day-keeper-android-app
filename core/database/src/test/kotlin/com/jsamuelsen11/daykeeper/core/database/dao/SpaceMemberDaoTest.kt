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
class SpaceMemberDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: SpaceMemberDao
  private lateinit var accountDao: AccountDao
  private lateinit var spaceDao: SpaceDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.spaceMemberDao()
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
  fun `upsert and getByCompositeKey returns entity`() = runTest {
    insertParents()
    val member = testSpaceMember()
    dao.upsert(member)
    dao.getByCompositeKey(spaceId = "space-1", tenantId = "tenant-1") shouldBe member
  }

  @Test
  fun `getByCompositeKey returns null when not found`() = runTest {
    dao.getByCompositeKey(spaceId = "missing", tenantId = "missing").shouldBeNull()
  }

  @Test
  fun `upsert updates existing entity`() = runTest {
    insertParents()
    dao.upsert(testSpaceMember(role = "OWNER"))
    val updated = testSpaceMember(role = "MEMBER")
    dao.upsert(updated)
    dao.getByCompositeKey(spaceId = "space-1", tenantId = "tenant-1")?.role shouldBe "MEMBER"
  }

  @Test
  fun `upsertAll inserts multiple members`() = runTest {
    accountDao.upsert(testAccount(tenantId = "t1"))
    accountDao.upsert(testAccount(tenantId = "t2"))
    accountDao.upsert(testAccount(tenantId = "t3"))
    spaceDao.upsert(testSpace(spaceId = "s1", tenantId = "t1"))
    dao.upsertAll(
      listOf(
        testSpaceMember(spaceId = "s1", tenantId = "t1"),
        testSpaceMember(spaceId = "s1", tenantId = "t2"),
        testSpaceMember(spaceId = "s1", tenantId = "t3"),
      )
    )
    dao.observeBySpace("s1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpace returns only non-deleted members`() = runTest {
    insertParents()
    accountDao.upsert(testAccount(tenantId = "t2"))
    dao.upsert(testSpaceMember(spaceId = "space-1", tenantId = "tenant-1"))
    dao.upsert(testSpaceMember(spaceId = "space-1", tenantId = "t2", deletedAt = 999L))
    dao.observeBySpace("space-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].tenantId shouldBe "tenant-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete marks member as deleted and hides it from observe`() = runTest {
    insertParents()
    dao.upsert(testSpaceMember())
    dao.softDelete(spaceId = "space-1", tenantId = "tenant-1", deletedAt = 999L, updatedAt = 1_000L)
    dao.observeBySpace("space-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getByCompositeKey(spaceId = "space-1", tenantId = "tenant-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes member`() = runTest {
    insertParents()
    dao.upsert(testSpaceMember())
    dao.hardDelete(spaceId = "space-1", tenantId = "tenant-1")
    dao.getByCompositeKey(spaceId = "space-1", tenantId = "tenant-1").shouldBeNull()
  }

  @Test
  fun `observeBySpace emits only members belonging to queried space`() = runTest {
    accountDao.upsert(testAccount(tenantId = "tenant-1"))
    spaceDao.upsert(testSpace(spaceId = "s1", tenantId = "tenant-1"))
    spaceDao.upsert(testSpace(spaceId = "s2", tenantId = "tenant-1"))
    dao.upsert(testSpaceMember(spaceId = "s1", tenantId = "tenant-1"))
    dao.upsert(testSpaceMember(spaceId = "s2", tenantId = "tenant-1"))
    dao.observeBySpace("s1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].spaceId shouldBe "s1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpace emits updates on mutation`() = runTest {
    accountDao.upsert(testAccount(tenantId = "t1"))
    accountDao.upsert(testAccount(tenantId = "t2"))
    spaceDao.upsert(testSpace(spaceId = "space-1", tenantId = "t1"))
    dao.observeBySpace("space-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testSpaceMember(spaceId = "space-1", tenantId = "t1"))
      awaitItem() shouldHaveSize 1
      dao.upsert(testSpaceMember(spaceId = "space-1", tenantId = "t2"))
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete preserves entity for getByCompositeKey`() = runTest {
    insertParents()
    dao.upsert(testSpaceMember())
    dao.softDelete(spaceId = "space-1", tenantId = "tenant-1", deletedAt = 888L, updatedAt = 900L)
    val member = dao.getByCompositeKey(spaceId = "space-1", tenantId = "tenant-1")
    member.shouldNotBeNull()
    member.deletedAt shouldBe 888L
    member.updatedAt shouldBe 900L
  }
}
