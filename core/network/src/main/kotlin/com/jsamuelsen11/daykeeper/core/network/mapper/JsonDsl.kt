package com.jsamuelsen11.daykeeper.core.network.mapper

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

private val isoFormatter = DateTimeFormatter.ISO_INSTANT

fun epochMsToIso(epochMs: Long): String =
  isoFormatter.format(Instant.ofEpochMilli(epochMs).atOffset(ZoneOffset.UTC))

fun isoToEpochMs(iso: String): Long = Instant.parse(iso).toEpochMilli()

inline fun jsonObj(block: JsonObjectBuilder.() -> Unit): JsonObject {
  val builder = JsonObjectBuilder()
  builder.block()
  return JsonObject(builder.map)
}

inline fun <T> JsonElement.obj(block: (JsonObjectAccessor) -> T): T =
  block(JsonObjectAccessor(jsonObject))

class JsonObjectBuilder {
  val map = mutableMapOf<String, JsonElement>()

  fun put(key: String, value: String) {
    map[key] = JsonPrimitive(value)
  }

  fun put(key: String, value: Boolean) {
    map[key] = JsonPrimitive(value)
  }

  fun put(key: String, value: Int) {
    map[key] = JsonPrimitive(value)
  }

  fun put(key: String, value: Long) {
    map[key] = JsonPrimitive(value)
  }

  fun put(key: String, value: Double) {
    map[key] = JsonPrimitive(value)
  }

  fun putOrNull(key: String, value: String?) {
    map[key] = if (value != null) JsonPrimitive(value) else JsonNull
  }

  fun putIntOrNull(key: String, value: Int?) {
    map[key] = if (value != null) JsonPrimitive(value) else JsonNull
  }

  fun putTimestamp(key: String, epochMs: Long) {
    map[key] = JsonPrimitive(epochMsToIso(epochMs))
  }

  fun putTimestampOrNull(key: String, epochMs: Long?) {
    map[key] = if (epochMs != null) JsonPrimitive(epochMsToIso(epochMs)) else JsonNull
  }
}

class JsonObjectAccessor(private val obj: JsonObject) {
  fun str(key: String): String = obj.getValue(key).jsonPrimitive.content

  fun strOrNull(key: String): String? {
    val el = obj[key] ?: return null
    return if (el is JsonNull) null else el.jsonPrimitive.content
  }

  fun bool(key: String): Boolean = obj.getValue(key).jsonPrimitive.boolean

  fun int(key: String): Int = obj.getValue(key).jsonPrimitive.int

  fun intOrNull(key: String): Int? {
    val el = obj[key] ?: return null
    return if (el is JsonNull) null else el.jsonPrimitive.intOrNull
  }

  fun long(key: String): Long = obj.getValue(key).jsonPrimitive.long

  fun longOrNull(key: String): Long? {
    val el = obj[key] ?: return null
    return if (el is JsonNull) null else el.jsonPrimitive.longOrNull
  }

  fun double(key: String): Double = obj.getValue(key).jsonPrimitive.double

  fun epochMs(key: String): Long = isoToEpochMs(str(key))

  fun epochMsOrNull(key: String): Long? = strOrNull(key)?.let { isoToEpochMs(it) }
}
