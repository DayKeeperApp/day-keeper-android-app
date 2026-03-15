package com.jsamuelsen11.daykeeper.core.database.converter

import androidx.room.TypeConverter
import java.util.UUID

/**
 * Room type converters for types that cannot be stored natively.
 *
 * Enums are stored as TEXT (their [Enum.name]) in entity fields and converted in the mapper layer,
 * keeping the database module decoupled from domain enum changes.
 */
class Converters {

  @TypeConverter fun uuidToString(value: UUID): String = value.toString()

  @TypeConverter fun stringToUuid(value: String): UUID = UUID.fromString(value)
}
