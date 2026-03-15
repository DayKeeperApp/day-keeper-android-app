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
class ImportantDateDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: ImportantDateDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.importantDateDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  private suspend fun insertParents(
    tenantId: String = "tenant-1",
    spaceId: String = "space-1",
    personId: String = "person-1",
  ) {
    db.accountDao().upsert(testAccount(tenantId = tenantId))
    db.spaceDao().upsert(testSpace(spaceId = spaceId, tenantId = tenantId))
    db.personDao().upsert(testPerson(personId = personId, spaceId = spaceId, tenantId = tenantId))
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    insertParents()
    val importantDate = testImportantDate()
    dao.upsert(importantDate)
    dao.getById(importantDate.importantDateId) shouldBe importantDate
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `getById returns soft-deleted entity`() = runTest {
    insertParents()
    dao.upsert(testImportantDate(deletedAt = 999L))
    dao.getById("idate-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `upsertAll inserts multiple important dates`() = runTest {
    insertParents()
    dao.upsertAll(
      listOf(
        testImportantDate(importantDateId = "idate-1"),
        testImportantDate(importantDateId = "idate-2"),
        testImportantDate(importantDateId = "idate-3"),
      )
    )
    dao.observeByPerson("person-1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByPerson emits only non-deleted important dates for that person`() = runTest {
    insertParents(personId = "person-1")
    db
      .personDao()
      .upsert(testPerson(personId = "person-2", spaceId = "space-1", tenantId = "tenant-1"))
    dao.upsert(testImportantDate(importantDateId = "idate-1", personId = "person-1"))
    dao.upsert(
      testImportantDate(importantDateId = "idate-2", personId = "person-1", deletedAt = 999L)
    )
    dao.upsert(testImportantDate(importantDateId = "idate-3", personId = "person-2"))
    dao.observeByPerson("person-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].importantDateId shouldBe "idate-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete hides important date from observe queries`() = runTest {
    insertParents()
    dao.upsert(testImportantDate())
    dao.softDelete("idate-1", deletedAt = 999L, updatedAt = 1_000L)
    dao.observeByPerson("person-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("idate-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes important date entirely`() = runTest {
    insertParents()
    dao.upsert(testImportantDate())
    dao.hardDelete("idate-1")
    dao.getById("idate-1").shouldBeNull()
  }

  @Test
  fun `observeByPerson emits update when important date is inserted`() = runTest {
    insertParents()
    dao.observeByPerson("person-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testImportantDate(importantDateId = "idate-1"))
      awaitItem() shouldHaveSize 1
      dao.upsert(testImportantDate(importantDateId = "idate-2"))
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }
}
