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
class DeviceDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: DeviceDao
  private lateinit var accountDao: AccountDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.deviceDao()
    accountDao = db.accountDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    accountDao.upsert(testAccount())
    val device = testDevice()
    dao.upsert(device)
    dao.getById(device.deviceId) shouldBe device
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    accountDao.upsert(testAccount())
    dao.upsert(testDevice())
    val updated = testDevice(deviceId = "device-1").copy(deviceName = "Updated Device")
    dao.upsert(updated)
    dao.getById("device-1")?.deviceName shouldBe "Updated Device"
  }

  @Test
  fun `upsertAll inserts multiple entities`() = runTest {
    accountDao.upsert(testAccount())
    dao.upsertAll(
      listOf(testDevice(deviceId = "d1"), testDevice(deviceId = "d2"), testDevice(deviceId = "d3"))
    )
    dao.observeByTenant("tenant-1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeById emits entity when present`() = runTest {
    accountDao.upsert(testAccount())
    dao.upsert(testDevice())
    dao.observeById("device-1").test {
      awaitItem().shouldNotBeNull().deviceId shouldBe "device-1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeById emits null when not found`() = runTest {
    dao.observeById("missing").test {
      awaitItem().shouldBeNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByTenant returns only devices for that tenant`() = runTest {
    accountDao.upsert(testAccount(tenantId = "t1"))
    accountDao.upsert(testAccount(tenantId = "t2"))
    dao.upsert(testDevice(deviceId = "d1", tenantId = "t1"))
    dao.upsert(testDevice(deviceId = "d2", tenantId = "t1"))
    dao.upsert(testDevice(deviceId = "d3", tenantId = "t2"))
    dao.observeByTenant("t1").test {
      val result = awaitItem()
      result shouldHaveSize 2
      result.all { it.tenantId == "t1" } shouldBe true
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `hardDelete removes entity`() = runTest {
    accountDao.upsert(testAccount())
    dao.upsert(testDevice())
    dao.hardDelete("device-1")
    dao.getById("device-1").shouldBeNull()
  }

  @Test
  fun `observeByTenant emits updates on mutation`() = runTest {
    accountDao.upsert(testAccount())
    dao.observeByTenant("tenant-1").test {
      awaitItem().shouldBeEmpty()
      dao.upsert(testDevice(deviceId = "d1"))
      awaitItem() shouldHaveSize 1
      dao.upsert(testDevice(deviceId = "d2"))
      awaitItem() shouldHaveSize 2
      cancelAndIgnoreRemainingEvents()
    }
  }
}
