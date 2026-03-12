package com.jsamuelsen11.daykeeper.core.model.list

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ShoppingListTest {

  private val shoppingList =
    ShoppingList(
      listId = "list-1",
      spaceId = "space-1",
      tenantId = "tenant-1",
      name = "Groceries",
      normalizedName = "groceries",
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    shoppingList.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `deletedAt defaults to null`() {
    shoppingList.deletedAt shouldBe null
  }
}
