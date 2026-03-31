package com.jsamuelsen11.daykeeper.core.network.dto.sync

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Maps to C# DayKeeper.Domain.Enums.ChangeLogEntityType. */
@Serializable(with = ChangeLogEntityTypeSerializer::class)
enum class ChangeLogEntityType(val value: Int) {
  TENANT(0),
  USER(1),
  SPACE(2),
  SPACE_MEMBERSHIP(3),
  CALENDAR(4),
  CALENDAR_EVENT(5),
  EVENT_TYPE(6),
  EVENT_REMINDER(7),
  TASK_ITEM(8),
  TASK_CATEGORY(9),
  CATEGORY(10),
  PROJECT(11),
  PERSON(12),
  CONTACT_METHOD(13),
  ADDRESS(14),
  IMPORTANT_DATE(15),
  SHOPPING_LIST(16),
  LIST_ITEM(17),
  ATTACHMENT(18),
  RECURRENCE_EXCEPTION(19),
  DEVICE(20),
  DEVICE_NOTIFICATION_PREFERENCE(21),
  UNKNOWN(-1);

  companion object {
    private val byValue = entries.associateBy { it.value }

    fun fromValue(value: Int): ChangeLogEntityType = byValue[value] ?: UNKNOWN
  }
}

internal object ChangeLogEntityTypeSerializer : KSerializer<ChangeLogEntityType> {
  override val descriptor = PrimitiveSerialDescriptor("ChangeLogEntityType", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: ChangeLogEntityType) {
    encoder.encodeInt(value.value)
  }

  override fun deserialize(decoder: Decoder): ChangeLogEntityType {
    return ChangeLogEntityType.fromValue(decoder.decodeInt())
  }
}
