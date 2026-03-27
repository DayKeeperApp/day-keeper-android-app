package com.jsamuelsen11.daykeeper.core.network.dto.sync

import kotlinx.serialization.Serializable

/** Response body from POST /api/v1/Sync/push. */
@Serializable
data class SyncPushResponseDto(
  val appliedCount: Int,
  val rejectedCount: Int,
  val conflicts: List<SyncConflictDto>,
)
