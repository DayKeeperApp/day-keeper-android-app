package com.jsamuelsen11.daykeeper.core.database.entity.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "devices",
  foreignKeys =
    [
      ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["tenant_id"],
        childColumns = ["tenant_id"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index(value = ["tenant_id"])],
)
public data class DeviceEntity(
  @PrimaryKey @ColumnInfo(name = "device_id") val deviceId: String,
  @ColumnInfo(name = "tenant_id") val tenantId: String,
  @ColumnInfo(name = "device_name") val deviceName: String,
  @ColumnInfo(name = "fcm_token") val fcmToken: String?,
  @ColumnInfo(name = "last_sync_cursor") val lastSyncCursor: Long = 0,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
