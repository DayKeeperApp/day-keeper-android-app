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
class TaskCategoryDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: TaskCategoryDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.taskCategoryDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    val category = testTaskCategory()
    dao.upsert(category)
    dao.getById(category.categoryId) shouldBe category
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    dao.upsert(testTaskCategory())
    val updated = testTaskCategory().copy(name = "Personal")
    dao.upsert(updated)
    dao.getById("cat-1")?.name shouldBe "Personal"
  }

  @Test
  fun `upsertAll inserts multiple entities`() = runTest {
    dao.upsertAll(
      listOf(
        testTaskCategory(categoryId = "c1", name = "Work"),
        testTaskCategory(categoryId = "c2", name = "Home"),
        testTaskCategory(categoryId = "c3", name = "Health"),
      )
    )
    dao.observeAll().test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeAll emits all categories`() = runTest {
    dao.upsert(testTaskCategory(categoryId = "c1", name = "Work"))
    dao.upsert(testTaskCategory(categoryId = "c2", name = "Home"))
    dao.observeAll().test {
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeAll emits empty list when no categories exist`() = runTest {
    dao.observeAll().test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeAll emits updates on mutation`() = runTest {
    dao.observeAll().test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testTaskCategory(categoryId = "c1"))
      awaitItem() shouldHaveSize 1
      dao.upsert(testTaskCategory(categoryId = "c2", name = "Home"))
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `hardDelete removes entity`() = runTest {
    dao.upsert(testTaskCategory())
    dao.hardDelete("cat-1")
    dao.getById("cat-1").shouldBeNull()
  }

  @Test
  fun `getById returns entity by id`() = runTest {
    dao.upsert(testTaskCategory(categoryId = "c1", name = "Work"))
    dao.upsert(testTaskCategory(categoryId = "c2", name = "Home"))
    dao.getById("c2").shouldNotBeNull().name shouldBe "Home"
  }
}
