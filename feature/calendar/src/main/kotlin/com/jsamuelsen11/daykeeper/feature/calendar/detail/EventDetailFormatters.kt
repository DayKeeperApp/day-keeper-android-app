package com.jsamuelsen11.daykeeper.feature.calendar.detail

import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceFrequency
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.model.calendar.ReminderPreset
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal const val MINUTES_PER_HOUR = 60
internal const val MINUTES_PER_DAY = 1_440
private const val WEEKDAY_ABBREV_LENGTH = 3

private val WEEKDAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a")

/**
 * Formats the date/time range of an event into a human-readable string.
 *
 * Examples:
 * - Timed, same day: `"Wed, Mar 19, 10:00 AM - 11:00 AM"`
 * - Timed, multi-day: `"Wed, Mar 19, 10:00 AM - Thu, Mar 20, 11:00 AM"`
 * - All-day, single day: `"Wed, Mar 19 (All day)"`
 * - All-day, multi-day: `"Wed, Mar 19 - Thu, Mar 20 (All day)"`
 *
 * @param zoneId Time zone used to convert epoch-millis to local date/time. Defaults to the system
 *   zone.
 */
fun Event.formatDateTimeRange(zoneId: ZoneId = ZoneId.systemDefault()): String {
  return if (isAllDay) {
    formatAllDayRange()
  } else {
    formatTimedRange(zoneId)
  }
}

private fun Event.formatAllDayRange(): String {
  val start = startDate?.let(LocalDate::parse) ?: return "(All day)"
  val end = endDate?.let(LocalDate::parse) ?: start
  val startFormatted = WEEKDAY_DATE_FORMATTER.format(start)
  return if (start == end) {
    "$startFormatted (All day)"
  } else {
    val endFormatted = WEEKDAY_DATE_FORMATTER.format(end)
    "$startFormatted - $endFormatted (All day)"
  }
}

private fun Event.formatTimedRange(zoneId: ZoneId): String {
  val startInstant = startAt?.let { Instant.ofEpochMilli(it) } ?: return ""
  val startZdt = startInstant.atZone(zoneId)
  val startDate = startZdt.toLocalDate()
  val startTime = TIME_FORMATTER.format(startZdt)
  val startFormatted = "${WEEKDAY_DATE_FORMATTER.format(startDate)}, $startTime"

  val endMillis = endAt
  val endSuffix =
    endMillis?.let { millis ->
      val endZdt = Instant.ofEpochMilli(millis).atZone(zoneId)
      val endTime = TIME_FORMATTER.format(endZdt)
      if (startDate == endZdt.toLocalDate()) {
        " - $endTime"
      } else {
        " - ${WEEKDAY_DATE_FORMATTER.format(endZdt.toLocalDate())}, $endTime"
      }
    } ?: ""

  return "$startFormatted$endSuffix"
}

/**
 * Returns a human-readable summary of this event's recurrence rule, or null when the event does not
 * repeat.
 *
 * Examples:
 * - `"Every day"`
 * - `"Every week"`
 * - `"Every 2 weeks on Mon, Wed"`
 * - `"Every month"`
 */
fun Event.formatRecurrenceSummary(): String? {
  val rule = recurrenceRule ?: return null
  return recurrenceSummary(rule)
}

private fun recurrenceSummary(rule: RecurrenceRule): String {
  val intervalPrefix =
    if (rule.interval == RecurrenceRule.DEFAULT_INTERVAL) "Every" else "Every ${rule.interval}"
  val frequencyLabel =
    when (rule.frequency) {
      RecurrenceFrequency.DAILY ->
        if (rule.interval == RecurrenceRule.DEFAULT_INTERVAL) "day" else "days"
      RecurrenceFrequency.WEEKLY ->
        if (rule.interval == RecurrenceRule.DEFAULT_INTERVAL) "week" else "weeks"
      RecurrenceFrequency.MONTHLY ->
        if (rule.interval == RecurrenceRule.DEFAULT_INTERVAL) "month" else "months"
      RecurrenceFrequency.YEARLY ->
        if (rule.interval == RecurrenceRule.DEFAULT_INTERVAL) "year" else "years"
    }
  val daysSuffix =
    if (rule.daysOfWeek.isNotEmpty()) {
      " on " +
        rule.daysOfWeek.joinToString(", ") { day ->
          day.name.lowercase().replaceFirstChar(Char::titlecase).take(WEEKDAY_ABBREV_LENGTH)
        }
    } else {
      ""
    }
  return "$intervalPrefix $frequencyLabel$daysSuffix"
}

/**
 * Returns a human-readable display string for this reminder.
 *
 * Uses the [ReminderPreset.displayLabel] when the [minutesBefore] value matches a preset exactly.
 * Falls back to a plain "X minutes before" style string otherwise.
 */
fun EventReminder.formatDisplay(): String {
  val preset = ReminderPreset.entries.find { it.minutesBefore == minutesBefore }
  if (preset != null) return preset.displayLabel
  return when {
    minutesBefore % MINUTES_PER_DAY == 0 -> {
      val days = minutesBefore / MINUTES_PER_DAY
      if (days == 1) "1 day before" else "$days days before"
    }
    minutesBefore % MINUTES_PER_HOUR == 0 -> {
      val hours = minutesBefore / MINUTES_PER_HOUR
      if (hours == 1) "1 hour before" else "$hours hours before"
    }
    else -> if (minutesBefore == 1) "1 minute before" else "$minutesBefore minutes before"
  }
}
