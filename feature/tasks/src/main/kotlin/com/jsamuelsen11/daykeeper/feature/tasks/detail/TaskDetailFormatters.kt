package com.jsamuelsen11.daykeeper.feature.tasks.detail

import androidx.compose.ui.graphics.Color
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceFrequency
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal const val MINUTES_PER_HOUR = 60
internal const val MINUTES_PER_DAY = 1_440
private const val HEX_RGB_LENGTH = 6
private const val HEX_ARGB_LENGTH = 8
private const val HEX_RADIX = 16

internal fun statusLabel(status: TaskStatus): String =
  when (status) {
    TaskStatus.TODO -> "To Do"
    TaskStatus.IN_PROGRESS -> "In Progress"
    TaskStatus.DONE -> "Done"
    TaskStatus.CANCELLED -> "Cancelled"
  }

internal fun formatEpochMillis(epochMillis: Long): String {
  val instant = Instant.ofEpochMilli(epochMillis)
  val formatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault())
  return formatter.format(instant)
}

internal fun recurrenceSummary(rule: RecurrenceRule): String {
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
        rule.daysOfWeek.joinToString(", ") { it.name.lowercase().replaceFirstChar(Char::titlecase) }
    } else {
      ""
    }
  return "$intervalPrefix $frequencyLabel$daysSuffix"
}

internal fun formatReminderMinutes(minutesBefore: Int): String =
  when {
    minutesBefore % MINUTES_PER_DAY == 0 -> {
      val days = minutesBefore / MINUTES_PER_DAY
      if (days == 1) "1 day before" else "$days days before"
    }
    minutesBefore % MINUTES_PER_HOUR == 0 -> {
      val hours = minutesBefore / MINUTES_PER_HOUR
      if (hours == 1) "1 hour before" else "$hours hours before"
    }
    else -> {
      if (minutesBefore == 1) "1 minute before" else "$minutesBefore minutes before"
    }
  }

internal fun parseHexColor(hex: String): Color? =
  runCatching {
      val cleaned = hex.trimStart('#')
      val argb =
        when (cleaned.length) {
          HEX_RGB_LENGTH -> "FF$cleaned".toLong(radix = HEX_RADIX)
          HEX_ARGB_LENGTH -> cleaned.toLong(radix = HEX_RADIX)
          else -> return@runCatching null
        }
      Color(argb.toInt())
    }
    .getOrNull()
