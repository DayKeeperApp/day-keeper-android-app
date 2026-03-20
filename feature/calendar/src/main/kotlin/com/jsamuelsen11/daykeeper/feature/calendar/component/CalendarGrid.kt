package com.jsamuelsen11.daykeeper.feature.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme
import java.time.LocalDate
import java.time.YearMonth

// region Constants

private val DayHeaderPadding = 4.dp
private val CellPadding = 2.dp
private val DotSize = 4.dp
private val DotSpacing = 2.dp
private val DotRowTopPadding = 2.dp
private val SelectedBorderWidth = 1.5f.dp
private const val OUT_OF_MONTH_ALPHA = 0.4f
private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")
private const val DAYS_IN_WEEK = 7
private const val PREVIEW_DOT_INTERVAL = 3
private const val PREVIEW_COLOR_GREEN = 0xFF4CAF50L
private const val PREVIEW_COLOR_BLUE = 0xFF2196F3L

// endregion

/**
 * Renders a month calendar grid with day-of-week column headers and tappable day cells.
 *
 * Each cell shows the day number, a primary-colored filled circle when it is today, an outlined
 * circle when it is the selected date, and up to [MAX_EVENT_DOTS] colored indicator dots for days
 * that have events. Days outside the current month are rendered at reduced opacity.
 *
 * @param monthData The pre-computed grid data for the month to render.
 * @param selectedDate The currently selected date, or `null` if none is selected.
 * @param onDateClick Callback invoked with the tapped [LocalDate].
 * @param modifier Modifier applied to the root [Column].
 */
@Composable
public fun CalendarMonthGrid(
  monthData: MonthGridData,
  selectedDate: LocalDate?,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    DayOfWeekHeader()
    monthData.weeks.forEach { week ->
      WeekRow(week = week, selectedDate = selectedDate, onDateClick = onDateClick)
    }
  }
}

@Composable
private fun DayOfWeekHeader() {
  Row(modifier = Modifier.fillMaxWidth()) {
    DAY_LABELS.forEach { label ->
      Text(
        text = label,
        modifier = Modifier.weight(1f).padding(vertical = DayHeaderPadding),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun WeekRow(
  week: List<DayCellData>,
  selectedDate: LocalDate?,
  onDateClick: (LocalDate) -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth()) {
    week.forEach { cell ->
      DayCell(
        cell = cell,
        isSelected = cell.date == selectedDate,
        onDateClick = onDateClick,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun DayCell(
  cell: DayCellData,
  isSelected: Boolean,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val alpha = if (cell.isCurrentMonth) 1f else OUT_OF_MONTH_ALPHA

  Box(
    modifier =
      modifier
        .aspectRatio(1f)
        .padding(CellPadding)
        .alpha(alpha)
        .clip(CircleShape)
        .clickable { onDateClick(cell.date) }
        .semantics { contentDescription = cell.date.toString() },
    contentAlignment = Alignment.Center,
  ) {
    DayCellBackground(isToday = cell.isToday, isSelected = isSelected)
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = cell.date.dayOfMonth.toString(),
        style = MaterialTheme.typography.bodySmall,
        color =
          when {
            cell.isToday -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurface
          },
      )
      if (cell.eventDots.isNotEmpty()) {
        Spacer(modifier = Modifier.height(DotRowTopPadding))
        EventDotRow(dots = cell.eventDots)
      }
    }
  }
}

@Composable
private fun DayCellBackground(isToday: Boolean, isSelected: Boolean) {
  when {
    isToday -> {
      Box(
        modifier =
          Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.primary)
      )
    }
    isSelected -> {
      Box(
        modifier =
          Modifier.fillMaxSize()
            .clip(CircleShape)
            .border(
              width = SelectedBorderWidth,
              color = MaterialTheme.colorScheme.primary,
              shape = CircleShape,
            )
      )
    }
    else -> Unit
  }
}

@Composable
private fun EventDotRow(dots: List<Color>) {
  Row(horizontalArrangement = Arrangement.spacedBy(DotSpacing)) {
    dots.take(MAX_EVENT_DOTS).forEach { color ->
      Box(modifier = Modifier.size(DotSize).clip(CircleShape).background(color))
    }
  }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun CalendarMonthGridPreview() {
  val today = LocalDate.now()
  val yearMonth = YearMonth.of(today.year, today.month)
  val firstOfMonth = yearMonth.atDay(1)
  // Monday=1 .. Sunday=7 in ISO; shift so week starts on Monday
  val firstDayOfWeek = firstOfMonth.dayOfWeek.value // 1=Mon
  val leadingDays = firstDayOfWeek - 1

  val cells = buildList {
    val prevMonth = yearMonth.minusMonths(1)
    val prevMonthDays = prevMonth.lengthOfMonth()
    repeat(leadingDays) { i ->
      val date = prevMonth.atDay(prevMonthDays - leadingDays + i + 1)
      add(
        DayCellData(date = date, isCurrentMonth = false, isToday = false, eventDots = emptyList())
      )
    }
    repeat(yearMonth.lengthOfMonth()) { i ->
      val date = yearMonth.atDay(i + 1)
      add(
        DayCellData(
          date = date,
          isCurrentMonth = true,
          isToday = date == today,
          eventDots =
            if (i % PREVIEW_DOT_INTERVAL == 0) {
              listOf(Color(PREVIEW_COLOR_GREEN), Color(PREVIEW_COLOR_BLUE))
            } else {
              emptyList()
            },
        )
      )
    }
    val totalCells = size
    val trailingDays = (DAYS_IN_WEEK - totalCells % DAYS_IN_WEEK) % DAYS_IN_WEEK
    val nextMonth = yearMonth.plusMonths(1)
    repeat(trailingDays) { i ->
      val date = nextMonth.atDay(i + 1)
      add(
        DayCellData(date = date, isCurrentMonth = false, isToday = false, eventDots = emptyList())
      )
    }
  }
  val weeks = cells.chunked(DAYS_IN_WEEK)
  val monthData = MonthGridData(yearMonth = yearMonth, weeks = weeks)

  DayKeeperTheme {
    CalendarMonthGrid(monthData = monthData, selectedDate = today, onDateClick = {})
  }
}

// endregion
