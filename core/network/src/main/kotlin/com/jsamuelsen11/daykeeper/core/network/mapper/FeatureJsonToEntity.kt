package com.jsamuelsen11.daykeeper.core.network.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.list.ShoppingListEntity
import com.jsamuelsen11.daykeeper.core.database.entity.list.ShoppingListItemEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.AddressEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.ContactMethodEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.ImportantDateEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.PersonEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.ProjectEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskCategoryEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskEntity
import kotlinx.serialization.json.JsonElement

fun JsonElement.toTaskEntity(): TaskEntity = obj { o ->
  TaskEntity(
    taskId = o.str("id"),
    projectId = o.strOrNull("projectId"),
    spaceId = o.str("spaceId"),
    tenantId = o.str("tenantId"),
    title = o.str("title"),
    description = o.strOrNull("description"),
    status = o.str("status"),
    priority = o.str("priority"),
    dueAt = o.epochMsOrNull("dueAt"),
    dueDate = o.strOrNull("dueDate"),
    recurrenceRule = o.strOrNull("recurrenceRule"),
    categoryId = o.strOrNull("categoryId"),
    reminderMinutesBefore = o.intOrNull("reminderMinutesBefore"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toTaskCategoryEntity(): TaskCategoryEntity = obj { o ->
  TaskCategoryEntity(
    categoryId = o.str("id"),
    name = o.str("name"),
    normalizedName = o.str("normalizedName"),
    isSystem = o.bool("isSystem"),
    color = o.strOrNull("color"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
  )
}

fun JsonElement.toProjectEntity(): ProjectEntity = obj { o ->
  ProjectEntity(
    projectId = o.str("id"),
    spaceId = o.str("spaceId"),
    tenantId = o.str("tenantId"),
    name = o.str("name"),
    normalizedName = o.str("normalizedName"),
    description = o.strOrNull("description"),
    status = o.str("status"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toPersonEntity(): PersonEntity = obj { o ->
  PersonEntity(
    personId = o.str("id"),
    spaceId = o.str("spaceId"),
    tenantId = o.str("tenantId"),
    firstName = o.str("firstName"),
    lastName = o.str("lastName"),
    nickname = o.strOrNull("nickname"),
    notes = o.strOrNull("notes"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toContactMethodEntity(): ContactMethodEntity = obj { o ->
  ContactMethodEntity(
    contactMethodId = o.str("id"),
    personId = o.str("personId"),
    type = o.str("type"),
    value = o.str("value"),
    label = o.str("label"),
    isPrimary = o.bool("isPrimary"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toAddressEntity(): AddressEntity = obj { o ->
  AddressEntity(
    addressId = o.str("id"),
    personId = o.str("personId"),
    label = o.str("label"),
    street = o.strOrNull("street1"),
    city = o.strOrNull("city"),
    state = o.strOrNull("state"),
    postalCode = o.strOrNull("postalCode"),
    country = o.strOrNull("country"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toImportantDateEntity(): ImportantDateEntity = obj { o ->
  ImportantDateEntity(
    importantDateId = o.str("id"),
    personId = o.str("personId"),
    label = o.str("label"),
    date = o.str("date"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toShoppingListEntity(): ShoppingListEntity = obj { o ->
  ShoppingListEntity(
    listId = o.str("id"),
    spaceId = o.str("spaceId"),
    tenantId = o.str("tenantId"),
    name = o.str("name"),
    normalizedName = o.str("normalizedName"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}

fun JsonElement.toShoppingListItemEntity(): ShoppingListItemEntity = obj { o ->
  ShoppingListItemEntity(
    itemId = o.str("id"),
    listId = o.str("shoppingListId"),
    name = o.str("name"),
    quantity = o.double("quantity"),
    unit = o.strOrNull("unit"),
    isChecked = o.bool("isChecked"),
    sortOrder = o.int("sortOrder"),
    createdAt = o.epochMs("createdAt"),
    updatedAt = o.epochMs("updatedAt"),
    deletedAt = o.epochMsOrNull("deletedAt"),
  )
}
