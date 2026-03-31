package com.jsamuelsen11.daykeeper.core.network.dto.sync

import kotlinx.serialization.Serializable

private const val DEFAULT_PULL_LIMIT = 500

/** Request body for POST /api/v1/Sync/pull. */
@Serializable
data class SyncPullRequestDto(
  val cursor: Long? = null,
  val spaceId: String? = null,
  val limit: Int? = DEFAULT_PULL_LIMIT,
)
