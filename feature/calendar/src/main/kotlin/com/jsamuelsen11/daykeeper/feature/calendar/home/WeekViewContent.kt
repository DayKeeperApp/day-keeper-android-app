package com.jsamuelsen11.daykeeper.feature.calendar.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.calendar.component.CalendarEventItem
import com.jsamuelsen11.daykeeper.feature.calendar.component.EventCard
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val DayHeaderPadding = 8.dp
private val ContentPadding = 16.dp
private val EventListSpacing = 8.dp
private const val DAYS_IN_WEEK = 7

@Composable
fun WeekViewContent(
  selectedDate: LocalDate,
  events: List<CalendarEventItem>,
  onEventClick: (String) -> Unit,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val weekStart = remember(selectedDate) { selectedDate.with(DayOfWeek.MONDAY) }
  val weekDays = remember(weekStart) { (0L until DAYS_IN_WEEK).map { weekStart.plusDays(it) } }

  Column(modifier = modifier.fillMaxSize()) {
    WeekDayHeaders(
      weekDays = weekDays,
      selectedDate = selectedDate,
      today = LocalDate.now(),
      onDateClick = onDateClick,
    )

    if (events.isEmpty()) {
      EmptyStateView(
        icon = DayKeeperIcons.Event,
        title = "No events",
        body = "Nothing scheduled for this day.",
        modifier = Modifier.fillMaxWidth().weight(1f),
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().weight(1f),
        contentPadding = PaddingValues(ContentPadding),
        verticalArrangement = Arrangement.spacedBy(EventListSpacing),
      ) {
        items(items = events, key = { it.event.eventId }) { item ->
          EventCard(
            item = item,
            onClick = { onEventClick(item.event.eventId) },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}

@Composable
private fun WeekDayHeaders(
  weekDays: List<LocalDate>,
  selectedDate: LocalDate,
  today: LocalDate,
  onDateClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier.fillMaxWidth().padding(horizontal = DayHeaderPadding)) {
    for (date in weekDays) {
      val isSelected = date == selectedDate
      val isToday = date == today
      val bgColor =
        when {
          isSelected -> MaterialTheme.colorScheme.primary
          isToday -> MaterialTheme.colorScheme.primaryContainer
          else -> MaterialTheme.colorScheme.surface
        }
      val textColor =
        when {
          isSelected -> MaterialTheme.colorScheme.onPrimary
          isToday -> MaterialTheme.colorScheme.onPrimaryContainer
          else -> MaterialTheme.colorScheme.onSurface
        }
      val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

      Box(
        modifier =
          Modifier.weight(1f)
            .clickable { onDateClick(date) }
            .background(bgColor)
            .padding(DayHeaderPadding),
        contentAlignment = Alignment.Center,
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            textAlign = TextAlign.Center,
          )
          Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            textAlign = TextAlign.Center,
          )
        }
      }
    }
  }
}
