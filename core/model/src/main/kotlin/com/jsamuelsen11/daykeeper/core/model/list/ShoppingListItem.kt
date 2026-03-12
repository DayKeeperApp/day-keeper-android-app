package com.jsamuelsen11.daykeeper.core.model.list

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** An item on a shopping list. */
data class ShoppingListItem(
  val itemId: String,
  val listId: String,
  val name: String,
  val quantity: Double = DEFAULT_QUANTITY,
  val unit: String? = null,
  val isChecked: Boolean,
  val sortOrder: Int,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel {

  companion object {
    const val DEFAULT_QUANTITY = 1.0
  }
}
