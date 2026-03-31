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
import kotlinx.serialization.json.JsonObject

fun TaskEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", taskId)
  putOrNull("projectId", projectId)
  put("spaceId", spaceId)
  put("tenantId", tenantId)
  put("title", title)
  putOrNull("description", description)
  put("status", status)
  put("priority", priority)
  putTimestampOrNull("dueAt", dueAt)
  putOrNull("dueDate", dueDate)
  putOrNull("recurrenceRule", recurrenceRule)
  putOrNull("categoryId", categoryId)
  putIntOrNull("reminderMinutesBefore", reminderMinutesBefore)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun TaskCategoryEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", categoryId)
  put("name", name)
  put("normalizedName", normalizedName)
  put("isSystem", isSystem)
  putOrNull("color", color)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
}

fun ProjectEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", projectId)
  put("spaceId", spaceId)
  put("tenantId", tenantId)
  put("name", name)
  put("normalizedName", normalizedName)
  putOrNull("description", description)
  put("status", status)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun PersonEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", personId)
  put("spaceId", spaceId)
  put("tenantId", tenantId)
  put("firstName", firstName)
  put("lastName", lastName)
  putOrNull("nickname", nickname)
  putOrNull("notes", notes)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun ContactMethodEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", contactMethodId)
  put("personId", personId)
  put("type", type)
  put("value", value)
  put("label", label)
  put("isPrimary", isPrimary)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun AddressEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", addressId)
  put("personId", personId)
  put("label", label)
  putOrNull("street1", street)
  putOrNull("city", city)
  putOrNull("state", state)
  putOrNull("postalCode", postalCode)
  putOrNull("country", country)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun ImportantDateEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", importantDateId)
  put("personId", personId)
  put("label", label)
  put("date", date)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun ShoppingListEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", listId)
  put("spaceId", spaceId)
  put("tenantId", tenantId)
  put("name", name)
  put("normalizedName", normalizedName)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}

fun ShoppingListItemEntity.toSyncJson(): JsonObject = jsonObj {
  put("id", itemId)
  put("shoppingListId", listId)
  put("name", name)
  put("quantity", quantity)
  putOrNull("unit", unit)
  put("isChecked", isChecked)
  put("sortOrder", sortOrder)
  putTimestamp("createdAt", createdAt)
  putTimestamp("updatedAt", updatedAt)
  putTimestampOrNull("deletedAt", deletedAt)
}
