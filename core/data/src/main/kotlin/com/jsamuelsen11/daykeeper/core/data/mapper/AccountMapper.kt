package com.jsamuelsen11.daykeeper.core.data.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity
import com.jsamuelsen11.daykeeper.core.database.entity.account.DeviceEntity
import com.jsamuelsen11.daykeeper.core.model.account.Account
import com.jsamuelsen11.daykeeper.core.model.account.Device
import com.jsamuelsen11.daykeeper.core.model.account.WeekStart

public fun AccountEntity.toDomain(): Account =
  Account(
    tenantId = tenantId,
    displayName = displayName,
    email = email,
    timezone = timezone,
    weekStart = WeekStart.valueOf(weekStart),
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun Account.toEntity(): AccountEntity =
  AccountEntity(
    tenantId = tenantId,
    displayName = displayName,
    email = email,
    timezone = timezone,
    weekStart = weekStart.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun DeviceEntity.toDomain(): Device =
  Device(
    deviceId = deviceId,
    tenantId = tenantId,
    deviceName = deviceName,
    fcmToken = fcmToken,
    lastSyncCursor = lastSyncCursor,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )

public fun Device.toEntity(): DeviceEntity =
  DeviceEntity(
    deviceId = deviceId,
    tenantId = tenantId,
    deviceName = deviceName,
    fcmToken = fcmToken,
    lastSyncCursor = lastSyncCursor,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )
