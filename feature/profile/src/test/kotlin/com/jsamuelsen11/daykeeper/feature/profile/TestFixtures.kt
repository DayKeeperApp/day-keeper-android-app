package com.jsamuelsen11.daykeeper.feature.profile

import com.jsamuelsen11.daykeeper.core.data.preferences.UserPreferences
import com.jsamuelsen11.daykeeper.core.model.account.Account
import com.jsamuelsen11.daykeeper.core.model.account.Device
import com.jsamuelsen11.daykeeper.core.model.account.WeekStart
import com.jsamuelsen11.daykeeper.core.model.space.Space
import com.jsamuelsen11.daykeeper.core.model.space.SpaceMember
import com.jsamuelsen11.daykeeper.core.model.space.SpaceRole
import com.jsamuelsen11.daykeeper.core.model.space.SpaceType
import com.jsamuelsen11.daykeeper.core.model.sync.SyncCursor

internal const val TEST_TENANT_ID = "default-tenant"
internal const val TEST_SPACE_ID = "test-space-id"
internal const val TEST_SPACE_ID_2 = "test-space-id-2"
internal const val TEST_DEVICE_ID = "test-device-id"
internal const val TEST_DEVICE_ID_2 = "test-device-id-2"
internal const val TEST_CREATED_AT = 1_000L
internal const val TEST_UPDATED_AT = 2_000L

internal fun makeAccount(
  tenantId: String = TEST_TENANT_ID,
  displayName: String = "Test User",
  email: String = "test@example.com",
  timezone: String = "America/New_York",
  weekStart: WeekStart = WeekStart.SUNDAY,
): Account =
  Account(
    tenantId = tenantId,
    displayName = displayName,
    email = email,
    timezone = timezone,
    weekStart = weekStart,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeSpace(
  spaceId: String = TEST_SPACE_ID,
  tenantId: String = TEST_TENANT_ID,
  name: String = "Personal",
  type: SpaceType = SpaceType.PERSONAL,
): Space =
  Space(
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = name.lowercase().trim(),
    type = type,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeSpaceMember(
  spaceId: String = TEST_SPACE_ID,
  tenantId: String = TEST_TENANT_ID,
  role: SpaceRole = SpaceRole.OWNER,
): SpaceMember =
  SpaceMember(
    spaceId = spaceId,
    tenantId = tenantId,
    role = role,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeDevice(
  deviceId: String = TEST_DEVICE_ID,
  tenantId: String = TEST_TENANT_ID,
  deviceName: String = "Pixel 8",
  lastSyncCursor: Long = 0L,
): Device =
  Device(
    deviceId = deviceId,
    tenantId = tenantId,
    deviceName = deviceName,
    lastSyncCursor = lastSyncCursor,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeSyncCursor(
  lastCursor: Long = 100L,
  lastSyncAt: Long = TEST_UPDATED_AT,
): SyncCursor = SyncCursor(lastCursor = lastCursor, lastSyncAt = lastSyncAt)

internal fun makePreferences(): UserPreferences = UserPreferences()
