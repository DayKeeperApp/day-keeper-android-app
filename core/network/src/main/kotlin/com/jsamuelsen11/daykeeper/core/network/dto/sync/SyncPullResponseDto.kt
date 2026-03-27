package com.jsamuelsen11.daykeeper.core.network.dto.sync

import kotlinx.serialization.Serializable

/** Response body from POST /api/v1/Sync/pull. */
@Serializable
data class SyncPullResponseDto(
  val changes: List<SyncChangeEntryDto>,
  val cursor: Long,
  val hasMore: Boolean,
)
