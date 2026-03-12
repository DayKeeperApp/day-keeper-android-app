package com.jsamuelsen11.daykeeper.core.model.account

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A registered device linked to an account. */
data class Device(
  val deviceId: String,
  val tenantId: String,
  val deviceName: String,
  val fcmToken: String? = null,
  val lastSyncCursor: Long = 0L,
  val createdAt: Long,
  val updatedAt: Long,
) : DayKeeperModel
