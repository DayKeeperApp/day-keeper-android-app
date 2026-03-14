package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.account.Device
import kotlinx.coroutines.flow.Flow

public interface DeviceRepository {
  public fun observeById(deviceId: String): Flow<Device?>

  public fun observeByTenant(tenantId: String): Flow<List<Device>>

  public suspend fun getById(deviceId: String): Device?

  public suspend fun upsert(device: Device)

  public suspend fun delete(deviceId: String)
}
