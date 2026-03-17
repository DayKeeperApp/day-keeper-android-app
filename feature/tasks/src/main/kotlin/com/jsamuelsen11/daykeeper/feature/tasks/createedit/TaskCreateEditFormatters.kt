package com.jsamuelsen11.daykeeper.feature.tasks.createedit

import com.jsamuelsen11.daykeeper.core.model.task.Priority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal const val MINUTES_PER_HOUR = 60
internal const val MINUTES_PER_DAY = 1_440
internal const val MILLIS_PER_MINUTE = 60_000L
private const val TIME_FORMAT_DISPLAY = "h:mm a"

internal fun priorityLabel(priority: Priority): String =
  when (priority) {
    Priority.NONE -> "None"
    Priority.LOW -> "Low"
    Priority.MEDIUM -> "Med"
    Priority.HIGH -> "High"
    Priority.URGENT -> "Urgent"
  }

internal fun formatEpochToDate(epochMillis: Long): String {
  val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  return format.format(Date(epochMillis))
}

internal fun formatEpochToTime(epochMillis: Long): String {
  val format = SimpleDateFormat(TIME_FORMAT_DISPLAY, Locale.getDefault())
  return format.format(Date(epochMillis))
}

internal fun formatReminderLabel(minutesBefore: Int): String =
  when {
    minutesBefore < MINUTES_PER_HOUR -> "$minutesBefore min before"
    minutesBefore % MINUTES_PER_DAY == 0 -> "${minutesBefore / MINUTES_PER_DAY} day(s) before"
    else -> "${minutesBefore / MINUTES_PER_HOUR} hr before"
  }

/**
 * Builds an epoch millis value from an ISO-8601 date string and clock hour/minute values. Returns
 * null if [dateString] is null or cannot be parsed.
 */
internal fun buildDueAtMillis(dateString: String?, hour: Int, minute: Int): Long? {
  val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  val datePart = dateString?.let { runCatching { format.parse(it) }.getOrNull() } ?: return null
  return datePart.time +
    hour.toLong() * MINUTES_PER_HOUR * MILLIS_PER_MINUTE +
    minute.toLong() * MILLIS_PER_MINUTE
}
