package com.jsamuelsen11.daykeeper.feature.calendar.component

import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Groups all event form callbacks into a single object to reduce parameter count on
 * [EventFormContent].
 */
public data class EventFormCallbacks(
  val onTitleChanged: (String) -> Unit,
  val onDescriptionChanged: (String) -> Unit,
  val onCalendarSelected: (String) -> Unit,
  val onEventTypeSelected: (String?) -> Unit,
  val onAllDayToggled: (Boolean) -> Unit,
  val onStartDateSelected: (String) -> Unit,
  val onStartTimeSelected: (Long) -> Unit,
  val onEndDateSelected: (String) -> Unit,
  val onEndTimeSelected: (Long) -> Unit,
  val onLocationChanged: (String) -> Unit,
  val onRecurrenceChanged: (RecurrenceRule?) -> Unit,
  val onAddReminder: (Int) -> Unit,
  val onRemoveReminder: (String) -> Unit,
  val onSave: () -> Unit,
)

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 1_440
private const val MINUTES_BEFORE_LAST_HOUR = 59
private const val MINUTES_BEFORE_LAST_SUBDAY = 1_439
private const val TIME_DISPLAY_PATTERN = "h:mm a"
private const val DATE_DISPLAY_PATTERN = "MMM d, yyyy"

internal fun formatEpochMillis(epochMillis: Long, timezone: String): String {
  val formatter =
    DateTimeFormatter.ofPattern(DATE_DISPLAY_PATTERN, Locale.getDefault())
      .withZone(ZoneId.of(timezone))
  return formatter.format(Instant.ofEpochMilli(epochMillis))
}

internal fun formatEpochMillisTime(epochMillis: Long, timezone: String): String {
  val formatter =
    DateTimeFormatter.ofPattern(TIME_DISPLAY_PATTERN, Locale.getDefault())
      .withZone(ZoneId.of(timezone))
  return formatter.format(Instant.ofEpochMilli(epochMillis))
}

internal fun epochMillisToIsoDate(epochMillis: Long, timezone: String): String {
  val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.of(timezone)).toLocalDate()
  return date.toString()
}

internal fun parseIsoDateToMillis(isoDate: String, timezone: String): Long {
  val date = LocalDate.parse(isoDate)
  return date.atStartOfDay(ZoneId.of(timezone)).toInstant().toEpochMilli()
}

internal fun combineDateAndTime(dateMillis: Long, hour: Int, minute: Int, timezone: String): Long {
  val date = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.of(timezone)).toLocalDate()
  return date.atTime(hour, minute).atZone(ZoneId.of(timezone)).toInstant().toEpochMilli()
}

internal fun formatReminderLabel(minutesBefore: Int): String =
  when (minutesBefore) {
    0 -> "At time of event"
    in 1..MINUTES_BEFORE_LAST_HOUR -> "$minutesBefore minutes before"
    MINUTES_PER_HOUR -> "1 hour before"
    in (MINUTES_PER_HOUR + 1)..MINUTES_BEFORE_LAST_SUBDAY ->
      "${minutesBefore / MINUTES_PER_HOUR} hours before"
    MINUTES_PER_DAY -> "1 day before"
    else -> "${minutesBefore / MINUTES_PER_DAY} days before"
  }
