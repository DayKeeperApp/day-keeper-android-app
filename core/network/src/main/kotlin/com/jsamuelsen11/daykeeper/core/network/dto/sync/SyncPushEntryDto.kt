package com.jsamuelsen11.daykeeper.core.network.dto.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** A single entity change pushed from the client to the server. */
@Serializable
data class SyncPushEntryDto(
  val entityType: ChangeLogEntityType,
  val entityId: String,
  val operation: ChangeOperation,
  val timestamp: String,
  val data: JsonElement? = null,
)
