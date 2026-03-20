package com.jsamuelsen11.daykeeper.feature.calendar.component

import androidx.compose.ui.graphics.Color
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import java.time.LocalDate
import java.time.YearMonth

/**
 * Aggregated data for rendering a single month in a calendar grid.
 *
 * @property yearMonth The month this grid represents.
 * @property weeks A list of 4–6 week rows, each containing exactly 7 [DayCellData] entries.
 */
public data class MonthGridData(val yearMonth: YearMonth, val weeks: List<List<DayCellData>>)

/**
 * Data for a single day cell in the month grid.
 *
 * @property date The calendar date this cell represents.
 * @property isCurrentMonth Whether the date belongs to the month being displayed. Days outside the
 *   current month (leading/trailing) are rendered with reduced opacity.
 * @property isToday Whether this date is today's date.
 * @property eventDots Up to [MAX_EVENT_DOTS] calendar colors representing events on this day.
 */
public data class DayCellData(
  val date: LocalDate,
  val isCurrentMonth: Boolean,
  val isToday: Boolean,
  val eventDots: List<Color>,
)

/**
 * An event enriched with calendar and event-type display metadata for use in lists and cards.
 *
 * @property event The raw domain event.
 * @property calendarName Human-readable name of the calendar this event belongs to.
 * @property calendarColor Hex color string (e.g. `"#4CAF50"`) of the parent calendar.
 * @property eventTypeName Human-readable event type name, or `null` when no type is set.
 */
public data class CalendarEventItem(
  val event: Event,
  val calendarName: String,
  val calendarColor: String,
  val eventTypeName: String?,
)

/** Maximum number of colored indicator dots shown per day cell. */
public const val MAX_EVENT_DOTS: Int = 3
