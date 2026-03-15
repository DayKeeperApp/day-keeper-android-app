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
class AddressDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: AddressDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.addressDao()
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
    val address = testAddress()
    dao.upsert(address)
    dao.getById(address.addressId) shouldBe address
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `getById returns soft-deleted entity`() = runTest {
    insertParents()
    dao.upsert(testAddress(deletedAt = 999L))
    dao.getById("addr-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `upsertAll inserts multiple addresses`() = runTest {
    insertParents()
    dao.upsertAll(
      listOf(
        testAddress(addressId = "addr-1"),
        testAddress(addressId = "addr-2"),
        testAddress(addressId = "addr-3"),
      )
    )
    dao.observeByPerson("person-1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByPerson emits only non-deleted addresses for that person`() = runTest {
    insertParents(personId = "person-1")
    db
      .personDao()
      .upsert(testPerson(personId = "person-2", spaceId = "space-1", tenantId = "tenant-1"))
    dao.upsert(testAddress(addressId = "addr-1", personId = "person-1"))
    dao.upsert(testAddress(addressId = "addr-2", personId = "person-1", deletedAt = 999L))
    dao.upsert(testAddress(addressId = "addr-3", personId = "person-2"))
    dao.observeByPerson("person-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].addressId shouldBe "addr-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `softDelete hides address from observe queries`() = runTest {
    insertParents()
    dao.upsert(testAddress())
    dao.softDelete("addr-1", deletedAt = 999L, updatedAt = 1_000L)
    dao.observeByPerson("person-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("addr-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes address entirely`() = runTest {
    insertParents()
    dao.upsert(testAddress())
    dao.hardDelete("addr-1")
    dao.getById("addr-1").shouldBeNull()
  }

  @Test
  fun `observeByPerson emits update when address is inserted`() = runTest {
    insertParents()
    dao.observeByPerson("person-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testAddress(addressId = "addr-1"))
      awaitItem() shouldHaveSize 1
      dao.upsert(testAddress(addressId = "addr-2"))
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }
}
