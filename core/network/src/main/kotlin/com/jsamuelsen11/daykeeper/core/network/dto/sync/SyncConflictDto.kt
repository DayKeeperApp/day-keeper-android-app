package com.jsamuelsen11.daykeeper.core.network.dto.sync

import kotlinx.serialization.Serializable

/** Describes why a pushed change was rejected by the server. */
@Serializable
data class SyncConflictDto(
  val entityType: ChangeLogEntityType,
  val entityId: String,
  val reason: SyncConflictReason,
  val clientTimestamp: String? = null,
  val serverTimestamp: String? = null,
)
