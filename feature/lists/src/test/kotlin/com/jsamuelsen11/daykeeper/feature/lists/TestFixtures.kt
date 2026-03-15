package com.jsamuelsen11.daykeeper.feature.lists

import com.jsamuelsen11.daykeeper.core.model.list.ShoppingList
import com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem

internal const val TEST_LIST_ID = "test-list-id"
internal const val TEST_LIST_ID_2 = "test-list-id-2"
internal const val TEST_SPACE_ID = "test-space-id"
internal const val TEST_TENANT_ID = "test-tenant-id"
internal const val TEST_ITEM_ID = "test-item-id"
internal const val TEST_ITEM_ID_2 = "test-item-id-2"
internal const val TEST_ITEM_ID_3 = "test-item-id-3"
internal const val TEST_CREATED_AT = 1_000L
internal const val TEST_UPDATED_AT = 2_000L
internal const val TEST_SORT_ORDER_FIRST = 0
internal const val TEST_SORT_ORDER_SECOND = 1
internal const val TEST_SORT_ORDER_THIRD = 2

internal fun makeList(
  listId: String = TEST_LIST_ID,
  name: String = "Groceries",
  spaceId: String = TEST_SPACE_ID,
  tenantId: String = TEST_TENANT_ID,
): ShoppingList =
  ShoppingList(
    listId = listId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = name.lowercase().trim(),
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeItem(
  itemId: String = TEST_ITEM_ID,
  listId: String = TEST_LIST_ID,
  name: String = "Milk",
  isChecked: Boolean = false,
  sortOrder: Int = TEST_SORT_ORDER_FIRST,
): ShoppingListItem =
  ShoppingListItem(
    itemId = itemId,
    listId = listId,
    name = name,
    isChecked = isChecked,
    sortOrder = sortOrder,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )
