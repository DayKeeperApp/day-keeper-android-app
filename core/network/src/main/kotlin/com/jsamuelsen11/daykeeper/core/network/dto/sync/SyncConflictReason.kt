package com.jsamuelsen11.daykeeper.core.network.dto.sync

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Maps to C# DayKeeper.Application.DTOs.Sync.SyncConflictReason. */
@Serializable(with = SyncConflictReasonSerializer::class)
enum class SyncConflictReason(val value: Int) {
  TIMESTAMP_CONFLICT(0),
  DUPLICATE_ENTITY(1),
  UNKNOWN(-1);

  companion object {
    private val byValue = entries.associateBy { it.value }

    fun fromValue(value: Int): SyncConflictReason = byValue[value] ?: UNKNOWN
  }
}

internal object SyncConflictReasonSerializer : KSerializer<SyncConflictReason> {
  override val descriptor = PrimitiveSerialDescriptor("SyncConflictReason", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: SyncConflictReason) {
    encoder.encodeInt(value.value)
  }

  override fun deserialize(decoder: Decoder): SyncConflictReason {
    return SyncConflictReason.fromValue(decoder.decodeInt())
  }
}
