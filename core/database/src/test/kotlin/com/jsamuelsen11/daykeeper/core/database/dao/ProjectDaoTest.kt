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
class ProjectDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: ProjectDao
  private lateinit var accountDao: AccountDao
  private lateinit var spaceDao: SpaceDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.projectDao()
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
    val project = testProject()
    dao.upsert(project)
    dao.getById(project.projectId) shouldBe project
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    insertParents()
    dao.upsert(testProject())
    val updated = testProject().copy(name = "Updated Project")
    dao.upsert(updated)
    dao.getById("proj-1")?.name shouldBe "Updated Project"
  }

  @Test
  fun `upsertAll inserts multiple entities`() = runTest {
    insertParents()
    dao.upsertAll(
      listOf(
        testProject(projectId = "p1"),
        testProject(projectId = "p2"),
        testProject(projectId = "p3"),
      )
    )
    dao.observeBySpace("space-1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeById emits entity when present`() = runTest {
    insertParents()
    dao.upsert(testProject())
    dao.observeById("proj-1").test {
      awaitItem().shouldNotBeNull().projectId shouldBe "proj-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeById filters soft-deleted entity`() = runTest {
    insertParents()
    dao.upsert(testProject(deletedAt = 999L))
    dao.observeById("proj-1").test {
      awaitItem().shouldBeNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpace returns only non-deleted projects for the space`() = runTest {
    insertParents(spaceId = "space-1")
    spaceDao.upsert(testSpace(spaceId = "space-2"))
    dao.upsert(testProject(projectId = "p1", spaceId = "space-1"))
    dao.upsert(testProject(projectId = "p2", spaceId = "space-1", deletedAt = 999L))
    dao.upsert(testProject(projectId = "p3", spaceId = "space-2"))
    dao.observeBySpace("space-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].projectId shouldBe "p1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete marks entity as deleted and hides it from observe`() = runTest {
    insertParents()
    dao.upsert(testProject())
    dao.softDelete("proj-1", deletedAt = 999L, updatedAt = 1000L)
    dao.observeBySpace("space-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("proj-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes entity entirely`() = runTest {
    insertParents()
    dao.upsert(testProject())
    dao.hardDelete("proj-1")
    dao.getById("proj-1").shouldBeNull()
  }
}
