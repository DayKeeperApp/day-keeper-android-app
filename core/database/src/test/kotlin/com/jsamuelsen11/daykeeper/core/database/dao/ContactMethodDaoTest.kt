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
class ContactMethodDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: ContactMethodDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.contactMethodDao()
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
    val contactMethod = testContactMethod()
    dao.upsert(contactMethod)
    dao.getById(contactMethod.contactMethodId) shouldBe contactMethod
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `getById returns soft-deleted entity`() = runTest {
    insertParents()
    dao.upsert(testContactMethod(deletedAt = 999L))
    dao.getById("cm-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `upsertAll inserts multiple contact methods`() = runTest {
    insertParents()
    dao.upsertAll(
      listOf(
        testContactMethod(contactMethodId = "cm-1"),
        testContactMethod(contactMethodId = "cm-2"),
        testContactMethod(contactMethodId = "cm-3"),
      )
    )
    dao.observeByPerson("person-1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByPerson emits only non-deleted contact methods for that person`() = runTest {
    insertParents(personId = "person-1")
    db
      .personDao()
      .upsert(testPerson(personId = "person-2", spaceId = "space-1", tenantId = "tenant-1"))
    dao.upsert(testContactMethod(contactMethodId = "cm-1", personId = "person-1"))
    dao.upsert(testContactMethod(contactMethodId = "cm-2", personId = "person-1", deletedAt = 999L))
    dao.upsert(testContactMethod(contactMethodId = "cm-3", personId = "person-2"))
    dao.observeByPerson("person-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].contactMethodId shouldBe "cm-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete hides contact method from observe queries`() = runTest {
    insertParents()
    dao.upsert(testContactMethod())
    dao.softDelete("cm-1", deletedAt = 999L, updatedAt = 1_000L)
    dao.observeByPerson("person-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("cm-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes contact method entirely`() = runTest {
    insertParents()
    dao.upsert(testContactMethod())
    dao.hardDelete("cm-1")
    dao.getById("cm-1").shouldBeNull()
  }

  @Test
  fun `observeByPerson emits update when contact method is inserted`() = runTest {
    insertParents()
    dao.observeByPerson("person-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testContactMethod(contactMethodId = "cm-1"))
      awaitItem() shouldHaveSize 1
      dao.upsert(testContactMethod(contactMethodId = "cm-2"))
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }
}
