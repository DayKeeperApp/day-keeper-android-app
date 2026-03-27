package com.jsamuelsen11.daykeeper.core.network.dto.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** A single change entry returned from the server during pull. */
@Serializable
data class SyncChangeEntryDto(
  val id: Long,
  val entityType: ChangeLogEntityType,
  val entityId: String,
  val operation: ChangeOperation,
  val timestamp: String,
  val data: JsonElement? = null,
)
