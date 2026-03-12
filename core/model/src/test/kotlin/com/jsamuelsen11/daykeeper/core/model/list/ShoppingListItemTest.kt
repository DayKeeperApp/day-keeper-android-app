package com.jsamuelsen11.daykeeper.core.model.list

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ShoppingListItemTest {

  private val item =
    ShoppingListItem(
      itemId = "item-1",
      listId = "list-1",
      name = "Milk",
      isChecked = false,
      sortOrder = 0,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    item.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `quantity defaults to 1`() {
    item.quantity shouldBe ShoppingListItem.DEFAULT_QUANTITY
  }

  @Test
  fun `unit defaults to null`() {
    item.unit shouldBe null
  }

  @Test
  fun `deletedAt defaults to null`() {
    item.deletedAt shouldBe null
  }
}
