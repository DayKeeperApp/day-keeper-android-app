package com.jsamuelsen11.daykeeper.core.data.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.list.ShoppingListEntity
import com.jsamuelsen11.daykeeper.core.database.entity.list.ShoppingListItemEntity
import com.jsamuelsen11.daykeeper.core.model.list.ShoppingList
import com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem

public fun ShoppingListEntity.toDomain(): ShoppingList =
  ShoppingList(
    listId = listId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = normalizedName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun ShoppingList.toEntity(): ShoppingListEntity =
  ShoppingListEntity(
    listId = listId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = normalizedName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun ShoppingListItemEntity.toDomain(): ShoppingListItem =
  ShoppingListItem(
    itemId = itemId,
    listId = listId,
    name = name,
    quantity = quantity,
    unit = unit,
    isChecked = isChecked,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun ShoppingListItem.toEntity(): ShoppingListItemEntity =
  ShoppingListItemEntity(
    itemId = itemId,
    listId = listId,
    name = name,
    quantity = quantity,
    unit = unit,
    isChecked = isChecked,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )
