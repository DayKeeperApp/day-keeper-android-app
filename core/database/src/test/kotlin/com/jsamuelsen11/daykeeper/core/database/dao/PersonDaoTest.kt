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
class PersonDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: PersonDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.personDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  private suspend fun insertParents(tenantId: String = "tenant-1", spaceId: String = "space-1") {
    db.accountDao().upsert(testAccount(tenantId = tenantId))
    db.spaceDao().upsert(testSpace(spaceId = spaceId, tenantId = tenantId))
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    insertParents()
    val person = testPerson()
    dao.upsert(person)
    dao.getById(person.personId) shouldBe person
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `getById returns soft-deleted entity`() = runTest {
    insertParents()
    dao.upsert(testPerson(deletedAt = 999L))
    dao.getById("person-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `upsertAll inserts multiple persons`() = runTest {
    insertParents()
    dao.upsertAll(
      listOf(testPerson(personId = "p1"), testPerson(personId = "p2"), testPerson(personId = "p3"))
    )
    dao.observeBySpace("space-1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeBySpace emits only non-deleted persons for that space`() = runTest {
    insertParents(tenantId = "tenant-1", spaceId = "space-1")
    db.spaceDao().upsert(testSpace(spaceId = "space-2", tenantId = "tenant-1"))
    dao.upsert(testPerson(personId = "p1", spaceId = "space-1"))
    dao.upsert(testPerson(personId = "p2", spaceId = "space-1", deletedAt = 999L))
    dao.upsert(testPerson(personId = "p3", spaceId = "space-2"))
    dao.observeBySpace("space-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].personId shouldBe "p1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeById filters soft-deleted person`() = runTest {
    insertParents()
    dao.upsert(testPerson(deletedAt = 999L))
    dao.observeById("person-1").test {
      awaitItem().shouldBeNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete hides person from observe queries`() = runTest {
    insertParents()
    dao.upsert(testPerson())
    dao.softDelete("person-1", deletedAt = 999L, updatedAt = 1_000L)
    dao.observeBySpace("space-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("person-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes person entirely`() = runTest {
    insertParents()
    dao.upsert(testPerson())
    dao.hardDelete("person-1")
    dao.getById("person-1").shouldBeNull()
  }

  @Test
  fun `observeBySpace emits update when person is inserted`() = runTest {
    insertParents()
    dao.observeBySpace("space-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testPerson(personId = "p1"))
      awaitItem() shouldHaveSize 1
      dao.upsert(testPerson(personId = "p2"))
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }
}
