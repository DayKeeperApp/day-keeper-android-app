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
class AttachmentDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: AttachmentDao
  private lateinit var accountDao: AccountDao
  private lateinit var spaceDao: SpaceDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.attachmentDao()
    accountDao = db.accountDao()
    spaceDao = db.spaceDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  private suspend fun insertParents() {
    accountDao.upsert(testAccount())
    spaceDao.upsert(testSpace())
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    insertParents()
    val attachment = testAttachment()
    dao.upsert(attachment)
    dao.getById(attachment.attachmentId) shouldBe attachment
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    insertParents()
    dao.upsert(testAttachment())
    val updated = testAttachment().copy(fileName = "updated.png")
    dao.upsert(updated)
    dao.getById("attach-1")?.fileName shouldBe "updated.png"
  }

  @Test
  fun `upsertAll inserts multiple entities`() = runTest {
    insertParents()
    dao.upsertAll(
      listOf(
        testAttachment(attachmentId = "a1", entityType = "TASK", entityId = "task-1"),
        testAttachment(attachmentId = "a2", entityType = "TASK", entityId = "task-1"),
      )
    )
    dao.observeByEntity("TASK", "task-1").test {
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByEntity filters by entityType and entityId`() = runTest {
    insertParents()
    dao.upsert(testAttachment(attachmentId = "a1", entityType = "TASK", entityId = "task-1"))
    dao.upsert(testAttachment(attachmentId = "a2", entityType = "TASK", entityId = "task-2"))
    dao.upsert(testAttachment(attachmentId = "a3", entityType = "PROJECT", entityId = "task-1"))
    dao.observeByEntity("TASK", "task-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].attachmentId shouldBe "a1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByEntity excludes soft-deleted`() = runTest {
    insertParents()
    dao.upsert(testAttachment(attachmentId = "a1", entityType = "TASK", entityId = "task-1"))
    dao.upsert(
      testAttachment(
        attachmentId = "a2",
        entityType = "TASK",
        entityId = "task-1",
        deletedAt = 999L,
      )
    )
    dao.observeByEntity("TASK", "task-1").test {
      awaitItem() shouldHaveSize 1
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByEntity emits updates on mutation`() = runTest {
    insertParents()
    dao.observeByEntity("TASK", "task-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testAttachment(entityType = "TASK", entityId = "task-1"))
      awaitItem() shouldHaveSize 1
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete marks entity as deleted`() = runTest {
    insertParents()
    dao.upsert(testAttachment(entityType = "TASK", entityId = "task-1"))
    dao.softDelete("attach-1", deletedAt = 999L, updatedAt = 1000L)
    dao.observeByEntity("TASK", "task-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("attach-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes entity entirely`() = runTest {
    insertParents()
    dao.upsert(testAttachment())
    dao.hardDelete("attach-1")
    dao.getById("attach-1").shouldBeNull()
  }
}
