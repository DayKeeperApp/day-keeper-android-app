package com.jsamuelsen11.daykeeper.core.model.list

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A shopping list within a space. */
data class ShoppingList(
  val listId: String,
  val spaceId: String,
  val tenantId: String,
  val name: String,
  val normalizedName: String,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
