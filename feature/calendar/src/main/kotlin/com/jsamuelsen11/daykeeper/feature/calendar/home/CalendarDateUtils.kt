package com.jsamuelsen11.daykeeper.feature.calendar.home

import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

private const val WEEKS_IN_GRID = 6
private const val DAYS_IN_WEEK = 7

/**
 * Returns the inclusive date range visible in a 6-week month grid where weeks start on Monday.
 *
 * The range begins on the Monday on or before the first day of the month and ends on the Sunday
 * that completes the sixth week row, ensuring the grid always has exactly 42 cells.
 */
fun YearMonth.visibleDateRange(): ClosedRange<LocalDate> {
  val firstOfMonth = atDay(1)
  val lastOfMonth = atEndOfMonth()
  val rangeStart = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
  val rangeEnd = rangeStart.plusDays((WEEKS_IN_GRID * DAYS_IN_WEEK - 1).toLong())
  // Ensure the range end is at least the last Sunday on or after the last of the month.
  val naturalEnd = lastOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
  return rangeStart..maxOf(rangeEnd, naturalEnd)
}

/**
 * Builds the 6-row by 7-column grid of [LocalDate]s for this month, starting on Monday.
 *
 * The returned list always contains exactly [WEEKS_IN_GRID] sublists of [DAYS_IN_WEEK] dates each.
 */
fun YearMonth.buildMonthGridWeeks(): List<List<LocalDate>> {
  val range = visibleDateRange()
  val start = range.start
  return (0 until WEEKS_IN_GRID).map { weekIndex ->
    (0 until DAYS_IN_WEEK).map { dayIndex ->
      start.plusDays((weekIndex * DAYS_IN_WEEK + dayIndex).toLong())
    }
  }
}

/**
 * Converts the [startAt] epoch-millis of a timed event to a [LocalDate] in the given [zoneId].
 *
 * @return The local date of the event's start instant, or null when [Event.startAt] is null.
 */
fun Event.toLocalDate(zoneId: ZoneId): LocalDate? =
  startAt?.let { epochMillisToLocalDate(it, zoneId) }

/**
 * Returns every [LocalDate] spanned by an all-day event using its [startDate] and [endDate]
 * ISO-8601 strings. Returns an empty list when either field is absent.
 */
fun Event.toLocalDates(zoneId: ZoneId): List<LocalDate> =
  if (!isAllDay) {
    toLocalDate(zoneId)?.let { listOf(it) } ?: emptyList()
  } else {
    allDayLocalDates()
  }

private fun Event.allDayLocalDates(): List<LocalDate> {
  val start = startDate?.let(LocalDate::parse) ?: return emptyList()
  val end = endDate?.let(LocalDate::parse) ?: start
  return generateSequence(start) { current ->
      val next = current.plusDays(1)
      if (next > end) null else next
    }
    .toList()
}

/**
 * Converts an epoch-millis timestamp to a [LocalDate] using the supplied [zoneId].
 *
 * @param epochMillis Milliseconds since the Unix epoch.
 * @param zoneId The time zone used for the conversion.
 */
fun epochMillisToLocalDate(epochMillis: Long, zoneId: ZoneId): LocalDate =
  Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
