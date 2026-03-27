package com.jsamuelsen11.daykeeper.core.network.dto.sync

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Maps to C# DayKeeper.Domain.Enums.ChangeOperation. */
@Serializable(with = ChangeOperationSerializer::class)
enum class ChangeOperation(val value: Int) {
  CREATED(0),
  UPDATED(1),
  DELETED(2),
  UNKNOWN(-1);

  companion object {
    private val byValue = entries.associateBy { it.value }

    fun fromValue(value: Int): ChangeOperation = byValue[value] ?: UNKNOWN
  }
}

internal object ChangeOperationSerializer : KSerializer<ChangeOperation> {
  override val descriptor = PrimitiveSerialDescriptor("ChangeOperation", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: ChangeOperation) {
    encoder.encodeInt(value.value)
  }

  override fun deserialize(decoder: Decoder): ChangeOperation {
    return ChangeOperation.fromValue(decoder.decodeInt())
  }
}
