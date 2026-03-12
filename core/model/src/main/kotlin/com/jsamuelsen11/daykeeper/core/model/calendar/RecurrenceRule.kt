package com.jsamuelsen11.daykeeper.core.model.calendar

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** Supported recurrence frequencies per RFC 5545. */
enum class RecurrenceFrequency(val rruleValue: String) {
  DAILY("DAILY"),
  WEEKLY("WEEKLY"),
  MONTHLY("MONTHLY"),
  YEARLY("YEARLY"),
}

/** Days of the week with RFC 5545 two-letter abbreviations. */
enum class RecurrenceDay(val rruleAbbrev: String) {
  MONDAY("MO"),
  TUESDAY("TU"),
  WEDNESDAY("WE"),
  THURSDAY("TH"),
  FRIDAY("FR"),
  SATURDAY("SA"),
  SUNDAY("SU"),
}

/** How a recurrence series ends. */
sealed class RecurrenceEndCondition {
  data object Never : RecurrenceEndCondition()

  data class AfterOccurrences(val count: Int) : RecurrenceEndCondition()

  data class UntilDate(val epochMillis: Long) : RecurrenceEndCondition()
}

/**
 * Domain model for a recurrence rule. Supports conversion to/from RFC 5545 RRULE strings for the
 * subset of features the app UI can produce.
 */
data class RecurrenceRule(
  val frequency: RecurrenceFrequency,
  val interval: Int = DEFAULT_INTERVAL,
  val daysOfWeek: Set<RecurrenceDay> = emptySet(),
  val endCondition: RecurrenceEndCondition = RecurrenceEndCondition.Never,
) : DayKeeperModel {

  fun toRruleString(): String = buildString {
    append("FREQ=")
    append(frequency.rruleValue)
    if (interval > DEFAULT_INTERVAL) {
      append(";INTERVAL=")
      append(interval)
    }
    if (daysOfWeek.isNotEmpty()) {
      append(";BYDAY=")
      append(daysOfWeek.joinToString(",") { it.rruleAbbrev })
    }
    when (endCondition) {
      is RecurrenceEndCondition.Never -> Unit
      is RecurrenceEndCondition.AfterOccurrences -> {
        append(";COUNT=")
        append(endCondition.count)
      }
      is RecurrenceEndCondition.UntilDate -> {
        append(";UNTIL=")
        append(endCondition.epochMillis)
      }
    }
  }

  companion object {
    const val DEFAULT_INTERVAL = 1

    private val dayLookup = RecurrenceDay.entries.associateBy { it.rruleAbbrev }
    private val freqLookup = RecurrenceFrequency.entries.associateBy { it.rruleValue }

    fun fromRruleString(rrule: String): RecurrenceRule {
      val parts =
        rrule.split(";").associate { part ->
          val (key, value) = part.split("=", limit = 2)
          key to value
        }

      val frequency =
        requireNotNull(freqLookup[parts["FREQ"]]) { "Missing or invalid FREQ in RRULE: $rrule" }

      val interval = parts["INTERVAL"]?.toIntOrNull() ?: DEFAULT_INTERVAL

      val daysOfWeek =
        parts["BYDAY"]?.split(",")?.mapNotNull { dayLookup[it.trim()] }?.toSet() ?: emptySet()

      val endCondition = parseEndCondition(parts, rrule)

      return RecurrenceRule(
        frequency = frequency,
        interval = interval,
        daysOfWeek = daysOfWeek,
        endCondition = endCondition,
      )
    }

    private fun parseEndCondition(
      parts: Map<String, String>,
      rrule: String,
    ): RecurrenceEndCondition =
      when {
        parts.containsKey("COUNT") -> {
          val count =
            requireNotNull(parts["COUNT"]?.toIntOrNull()) { "Invalid COUNT in RRULE: $rrule" }
          RecurrenceEndCondition.AfterOccurrences(count)
        }
        parts.containsKey("UNTIL") -> {
          val until =
            requireNotNull(parts["UNTIL"]?.toLongOrNull()) { "Invalid UNTIL in RRULE: $rrule" }
          RecurrenceEndCondition.UntilDate(until)
        }
        else -> RecurrenceEndCondition.Never
      }
  }
}
