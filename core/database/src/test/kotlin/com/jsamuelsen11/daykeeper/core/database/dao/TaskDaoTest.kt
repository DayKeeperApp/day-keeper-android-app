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
class TaskDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: TaskDao
  private lateinit var accountDao: AccountDao
  private lateinit var spaceDao: SpaceDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.taskDao()
    accountDao = db.accountDao()
    spaceDao = db.spaceDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  private suspend fun insertParents(spaceId: String = "space-1", tenantId: String = "tenant-1") {
    accountDao.upsert(testAccount(tenantId = tenantId))
    spaceDao.upsert(testSpace(spaceId = spaceId, tenantId = tenantId))
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    insertParents()
    val task = testTask()
    dao.upsert(task)
    dao.getById(task.taskId) shouldBe task
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    insertParents()
    dao.upsert(testTask())
    val updated = testTask().copy(title = "Updated Task")
    dao.upsert(updated)
    dao.getById("task-1")?.title shouldBe "Updated Task"
  }

  @Test
  fun `upsertAll inserts multiple entities`() = runTest {
    insertParents()
    dao.upsertAll(listOf(testTask(taskId = "t1"), testTask(taskId = "t2"), testTask(taskId = "t3")))
    dao.observeBySpace("space-1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeById emits entity when present`() = runTest {
    insertParents()
    dao.upsert(testTask())
    dao.observeById("task-1").test {
      awaitItem().shouldNotBeNull().taskId shouldBe "task-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeById filters soft-deleted entity`() = runTest {
    insertParents()
    dao.upsert(testTask(deletedAt = 999L))
    dao.observeById("task-1").test {
      awaitItem().shouldBeNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpace returns only non-deleted tasks for the space`() = runTest {
    insertParents(spaceId = "space-1")
    spaceDao.upsert(testSpace(spaceId = "space-2"))
    dao.upsert(testTask(taskId = "t1", spaceId = "space-1"))
    dao.upsert(testTask(taskId = "t2", spaceId = "space-1", deletedAt = 999L))
    dao.upsert(testTask(taskId = "t3", spaceId = "space-2"))
    dao.observeBySpace("space-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].taskId shouldBe "t1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpaceAndStatus filters by single status`() = runTest {
    insertParents()
    dao.upsert(testTask(taskId = "t1", status = "TODO"))
    dao.upsert(testTask(taskId = "t2", status = "IN_PROGRESS"))
    dao.upsert(testTask(taskId = "t3", status = "DONE"))
    dao.observeBySpaceAndStatus("space-1", listOf("TODO")).test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].taskId shouldBe "t1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpaceAndStatus filters by multiple statuses`() = runTest {
    insertParents()
    dao.upsert(testTask(taskId = "t1", status = "TODO"))
    dao.upsert(testTask(taskId = "t2", status = "IN_PROGRESS"))
    dao.upsert(testTask(taskId = "t3", status = "DONE"))
    dao.observeBySpaceAndStatus("space-1", listOf("TODO", "IN_PROGRESS")).test {
      val result = awaitItem()
      result shouldHaveSize 2
      result.map { it.taskId }.toSet() shouldBe setOf("t1", "t2")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpaceAndStatus excludes soft-deleted tasks`() = runTest {
    insertParents()
    dao.upsert(testTask(taskId = "t1", status = "TODO"))
    dao.upsert(testTask(taskId = "t2", status = "TODO", deletedAt = 999L))
    dao.observeBySpaceAndStatus("space-1", listOf("TODO")).test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].taskId shouldBe "t1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpaceAndStatus orders by due_at ascending`() = runTest {
    insertParents()
    dao.upsert(testTask(taskId = "t1", status = "TODO", dueAt = 300_000L))
    dao.upsert(testTask(taskId = "t2", status = "TODO", dueAt = 100_000L))
    dao.upsert(testTask(taskId = "t3", status = "TODO", dueAt = 200_000L))
    dao.observeBySpaceAndStatus("space-1", listOf("TODO")).test {
      val result = awaitItem()
      result.map { it.taskId } shouldBe listOf("t2", "t3", "t1")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByProject returns only non-deleted tasks for the project`() = runTest {
    insertParents()
    val projectDao = db.projectDao()
    projectDao.upsert(testProject(projectId = "proj-1"))
    projectDao.upsert(testProject(projectId = "proj-2"))
    dao.upsert(testTask(taskId = "t1", projectId = "proj-1"))
    dao.upsert(testTask(taskId = "t2", projectId = "proj-1", deletedAt = 999L))
    dao.upsert(testTask(taskId = "t3", projectId = "proj-2"))
    dao.observeByProject("proj-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].taskId shouldBe "t1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete marks entity as deleted and hides it from observe`() = runTest {
    insertParents()
    dao.upsert(testTask())
    dao.softDelete("task-1", deletedAt = 999L, updatedAt = 1000L)
    dao.observeBySpace("space-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("task-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes entity entirely`() = runTest {
    insertParents()
    dao.upsert(testTask())
    dao.hardDelete("task-1")
    dao.getById("task-1").shouldBeNull()
  }
}
