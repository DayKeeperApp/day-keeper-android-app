package com.jsamuelsen11.daykeeper.core.database.entity.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
public data class AccountEntity(
  @PrimaryKey @ColumnInfo(name = "tenant_id") val tenantId: String,
  @ColumnInfo(name = "display_name") val displayName: String,
  @ColumnInfo(name = "email") val email: String,
  @ColumnInfo(name = "timezone") val timezone: String,
  @ColumnInfo(name = "week_start") val weekStart: String,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
