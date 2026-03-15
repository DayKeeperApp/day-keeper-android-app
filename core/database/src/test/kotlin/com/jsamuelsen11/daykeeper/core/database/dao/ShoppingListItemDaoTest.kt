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
class ShoppingListItemDaoTest {

  private lateinit var db: DayKeeperDatabase
  private lateinit var dao: ShoppingListItemDao
  private lateinit var accountDao: AccountDao
  private lateinit var spaceDao: SpaceDao
  private lateinit var shoppingListDao: ShoppingListDao

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, DayKeeperDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    dao = db.shoppingListItemDao()
    accountDao = db.accountDao()
    spaceDao = db.spaceDao()
    shoppingListDao = db.shoppingListDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  private suspend fun insertParents() {
    accountDao.upsert(testAccount())
    spaceDao.upsert(testSpace())
    shoppingListDao.upsert(testShoppingList())
  }

  @Test
  fun `upsert and getById returns entity`() = runTest {
    insertParents()
    val item = testShoppingListItem()
    dao.upsert(item)
    dao.getById(item.itemId) shouldBe item
  }

  @Test
  fun `getById returns null when not found`() = runTest { dao.getById("missing").shouldBeNull() }

  @Test
  fun `upsert updates existing entity`() = runTest {
    insertParents()
    dao.upsert(testShoppingListItem())
    val updated = testShoppingListItem().copy(name = "Oat Milk")
    dao.upsert(updated)
    dao.getById("item-1")?.name shouldBe "Oat Milk"
  }

  @Test
  fun `upsertAll inserts multiple entities`() = runTest {
    insertParents()
    dao.upsertAll(
      listOf(
        testShoppingListItem(itemId = "i1", name = "Milk"),
        testShoppingListItem(itemId = "i2", name = "Eggs"),
        testShoppingListItem(itemId = "i3", name = "Bread"),
      )
    )
    dao.observeByList("list-1").test {
      awaitItem() shouldHaveSize 3
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByList excludes soft-deleted items`() = runTest {
    insertParents()
    dao.upsert(testShoppingListItem(itemId = "i1", name = "Milk"))
    dao.upsert(testShoppingListItem(itemId = "i2", name = "Eggs", deletedAt = 999L))
    dao.observeByList("list-1").test {
      val result = awaitItem()
      result shouldHaveSize 1
      result[0].itemId shouldBe "i1"
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `observeByList orders unchecked before checked then by sort_order`() = runTest {
    insertParents()
    dao.upsert(testShoppingListItem(itemId = "i1", isChecked = true, sortOrder = 0))
    dao.upsert(testShoppingListItem(itemId = "i2", isChecked = false, sortOrder = 1))
    dao.upsert(testShoppingListItem(itemId = "i3", isChecked = false, sortOrder = 0))
    dao.observeByList("list-1").test {
      val result = awaitItem()
      result.map { it.itemId } shouldBe listOf("i3", "i2", "i1")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `toggleChecked sets isChecked to true`() = runTest {
    insertParents()
    dao.upsert(testShoppingListItem(isChecked = false))
    dao.toggleChecked("item-1", isChecked = true, updatedAt = 5_000L)
    dao.getById("item-1")?.isChecked shouldBe true
  }

  @Test
  fun `toggleChecked sets isChecked to false`() = runTest {
    insertParents()
    dao.upsert(testShoppingListItem(isChecked = true))
    dao.toggleChecked("item-1", isChecked = false, updatedAt = 5_000L)
    dao.getById("item-1")?.isChecked shouldBe false
  }

  @Test
  fun `softDelete marks entity as deleted`() = runTest {
    insertParents()
    dao.upsert(testShoppingListItem())
    dao.softDelete("item-1", deletedAt = 999L, updatedAt = 1000L)
    dao.observeByList("list-1").test {
      awaitItem().shouldBeEmpty()
      cancelAndIgnoreRemainingEvents()
    }
    dao.getById("item-1")?.deletedAt shouldBe 999L
  }

  @Test
  fun `hardDelete removes entity entirely`() = runTest {
    insertParents()
    dao.upsert(testShoppingListItem())
    dao.hardDelete("item-1")
    dao.getById("item-1").shouldBeNull()
  }
}
