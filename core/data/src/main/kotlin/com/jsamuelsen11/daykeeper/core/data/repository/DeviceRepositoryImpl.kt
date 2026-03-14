package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.DeviceDao
import com.jsamuelsen11.daykeeper.core.model.account.Device
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class DeviceRepositoryImpl(private val dao: DeviceDao) : DeviceRepository {
  public override fun observeById(deviceId: String): Flow<Device?> =
    dao.observeById(deviceId).map { it?.toDomain() }

  public override fun observeByTenant(tenantId: String): Flow<List<Device>> =
    dao.observeByTenant(tenantId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(deviceId: String): Device? = dao.getById(deviceId)?.toDomain()

  public override suspend fun upsert(device: Device) {
    dao.upsert(device.toEntity())
  }

  public override suspend fun delete(deviceId: String) {
    dao.hardDelete(deviceId)
  }
}
