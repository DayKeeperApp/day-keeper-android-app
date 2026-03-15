package com.jsamuelsen11.daykeeper.core.database.dao

import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity
import com.jsamuelsen11.daykeeper.core.database.entity.account.DeviceEntity
import com.jsamuelsen11.daykeeper.core.database.entity.attachment.AttachmentEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.CalendarEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventReminderEntity
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventTypeEntity
import com.jsamuelsen11.daykeeper.core.database.entity.list.ShoppingListEntity
import com.jsamuelsen11.daykeeper.core.database.entity.list.ShoppingListItemEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.AddressEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.ContactMethodEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.ImportantDateEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.PersonEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceMemberEntity
import com.jsamuelsen11.daykeeper.core.database.entity.sync.SyncCursorEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.ProjectEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskCategoryEntity
import com.jsamuelsen11.daykeeper.core.database.entity.task.TaskEntity

internal fun testAccount(
  tenantId: String = "tenant-1",
  displayName: String = "Test User",
  email: String = "test@example.com",
  deletedAt: Long? = null,
) =
  AccountEntity(
    tenantId = tenantId,
    displayName = displayName,
    email = email,
    timezone = "America/New_York",
    weekStart = "SUNDAY",
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testDevice(deviceId: String = "device-1", tenantId: String = "tenant-1") =
  DeviceEntity(
    deviceId = deviceId,
    tenantId = tenantId,
    deviceName = "Test Device",
    fcmToken = null,
    lastSyncCursor = 0,
    createdAt = 1_000L,
    updatedAt = 2_000L,
  )

internal fun testSpace(
  spaceId: String = "space-1",
  tenantId: String = "tenant-1",
  name: String = "Test Space",
  deletedAt: Long? = null,
) =
  SpaceEntity(
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = name.lowercase(),
    type = "PERSONAL",
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testSpaceMember(
  spaceId: String = "space-1",
  tenantId: String = "tenant-1",
  role: String = "OWNER",
  deletedAt: Long? = null,
) =
  SpaceMemberEntity(
    spaceId = spaceId,
    tenantId = tenantId,
    role = role,
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testCalendar(
  calendarId: String = "cal-1",
  spaceId: String = "space-1",
  tenantId: String = "tenant-1",
  name: String = "Default",
  deletedAt: Long? = null,
) =
  CalendarEntity(
    calendarId = calendarId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = name.lowercase(),
    color = "#FF0000",
    isDefault = true,
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testEventType(eventTypeId: String = "etype-1", name: String = "Meeting") =
  EventTypeEntity(
    eventTypeId = eventTypeId,
    name = name,
    normalizedName = name.lowercase(),
    isSystem = true,
    color = "#0000FF",
    createdAt = 1_000L,
    updatedAt = 2_000L,
  )

internal fun testEvent(
  eventId: String = "event-1",
  calendarId: String = "cal-1",
  spaceId: String = "space-1",
  tenantId: String = "tenant-1",
  title: String = "Test Event",
  startAt: Long? = 100_000L,
  endAt: Long? = 200_000L,
  startDate: String? = null,
  endDate: String? = null,
  isAllDay: Boolean = false,
  eventTypeId: String? = null,
  deletedAt: Long? = null,
) =
  EventEntity(
    eventId = eventId,
    calendarId = calendarId,
    spaceId = spaceId,
    tenantId = tenantId,
    title = title,
    description = null,
    startAt = startAt,
    endAt = endAt,
    startDate = startDate,
    endDate = endDate,
    isAllDay = isAllDay,
    timezone = "America/New_York",
    eventTypeId = eventTypeId,
    location = null,
    recurrenceRule = null,
    parentEventId = null,
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testEventReminder(
  reminderId: String = "reminder-1",
  eventId: String = "event-1",
  minutesBefore: Int = 15,
  deletedAt: Long? = null,
) =
  EventReminderEntity(
    reminderId = reminderId,
    eventId = eventId,
    minutesBefore = minutesBefore,
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testPerson(
  personId: String = "person-1",
  spaceId: String = "space-1",
  tenantId: String = "tenant-1",
  firstName: String = "John",
  lastName: String = "Doe",
  deletedAt: Long? = null,
) =
  PersonEntity(
    personId = personId,
    spaceId = spaceId,
    tenantId = tenantId,
    firstName = firstName,
    lastName = lastName,
    nickname = null,
    notes = null,
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testContactMethod(
  contactMethodId: String = "cm-1",
  personId: String = "person-1",
  deletedAt: Long? = null,
) =
  ContactMethodEntity(
    contactMethodId = contactMethodId,
    personId = personId,
    type = "EMAIL",
    value = "john@example.com",
    label = "Work",
    isPrimary = true,
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testAddress(
  addressId: String = "addr-1",
  personId: String = "person-1",
  deletedAt: Long? = null,
) =
  AddressEntity(
    addressId = addressId,
    personId = personId,
    label = "Home",
    street = "123 Main St",
    city = "Anytown",
    state = "NY",
    postalCode = "10001",
    country = "US",
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testImportantDate(
  importantDateId: String = "idate-1",
  personId: String = "person-1",
  deletedAt: Long? = null,
) =
  ImportantDateEntity(
    importantDateId = importantDateId,
    personId = personId,
    label = "Birthday",
    date = "1990-01-15",
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testProject(
  projectId: String = "proj-1",
  spaceId: String = "space-1",
  tenantId: String = "tenant-1",
  deletedAt: Long? = null,
) =
  ProjectEntity(
    projectId = projectId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = "Test Project",
    normalizedName = "test project",
    description = null,
    status = "ACTIVE",
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testTaskCategory(categoryId: String = "cat-1", name: String = "Work") =
  TaskCategoryEntity(
    categoryId = categoryId,
    name = name,
    normalizedName = name.lowercase(),
    isSystem = false,
    color = "#00FF00",
    createdAt = 1_000L,
    updatedAt = 2_000L,
  )

internal fun testTask(
  taskId: String = "task-1",
  projectId: String? = null,
  spaceId: String = "space-1",
  tenantId: String = "tenant-1",
  title: String = "Test Task",
  status: String = "TODO",
  dueAt: Long? = null,
  categoryId: String? = null,
  deletedAt: Long? = null,
) =
  TaskEntity(
    taskId = taskId,
    projectId = projectId,
    spaceId = spaceId,
    tenantId = tenantId,
    title = title,
    description = null,
    status = status,
    priority = "MEDIUM",
    dueAt = dueAt,
    dueDate = null,
    recurrenceRule = null,
    categoryId = categoryId,
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testShoppingList(
  listId: String = "list-1",
  spaceId: String = "space-1",
  tenantId: String = "tenant-1",
  deletedAt: Long? = null,
) =
  ShoppingListEntity(
    listId = listId,
    spaceId = spaceId,
    tenantId = tenantId,
    name = "Groceries",
    normalizedName = "groceries",
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testShoppingListItem(
  itemId: String = "item-1",
  listId: String = "list-1",
  name: String = "Milk",
  isChecked: Boolean = false,
  sortOrder: Int = 0,
  deletedAt: Long? = null,
) =
  ShoppingListItemEntity(
    itemId = itemId,
    listId = listId,
    name = name,
    quantity = 1.0,
    unit = "gallon",
    isChecked = isChecked,
    sortOrder = sortOrder,
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testAttachment(
  attachmentId: String = "attach-1",
  entityType: String = "PERSON",
  entityId: String = "person-1",
  spaceId: String = "space-1",
  tenantId: String = "tenant-1",
  deletedAt: Long? = null,
) =
  AttachmentEntity(
    attachmentId = attachmentId,
    entityType = entityType,
    entityId = entityId,
    tenantId = tenantId,
    spaceId = spaceId,
    fileName = "photo.jpg",
    mimeType = "image/jpeg",
    fileSize = 1024L,
    remoteUrl = null,
    localPath = "/data/photo.jpg",
    createdAt = 1_000L,
    updatedAt = 2_000L,
    deletedAt = deletedAt,
  )

internal fun testSyncCursor(
  id: String = "sync_cursor",
  lastCursor: Long = 100L,
  lastSyncAt: Long = 5_000L,
) = SyncCursorEntity(id = id, lastCursor = lastCursor, lastSyncAt = lastSyncAt)
