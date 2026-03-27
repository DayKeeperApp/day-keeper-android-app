package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.account.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface DeviceDao {

  @Query("SELECT * FROM devices WHERE device_id = :deviceId")
  public fun observeById(deviceId: String): Flow<DeviceEntity?>

  @Query("SELECT * FROM devices WHERE tenant_id = :tenantId")
  public fun observeByTenant(tenantId: String): Flow<List<DeviceEntity>>

  @Query("SELECT * FROM devices WHERE device_id = :deviceId")
  public suspend fun getById(deviceId: String): DeviceEntity?

  @Upsert public suspend fun upsert(entity: DeviceEntity)

  @Upsert public suspend fun upsertAll(entities: List<DeviceEntity>)

  @Query("DELETE FROM devices WHERE device_id = :deviceId")
  public suspend fun hardDelete(deviceId: String)

  @Query("SELECT * FROM devices WHERE updated_at > :since")
  public suspend fun getModifiedSince(since: Long): List<DeviceEntity>
}
