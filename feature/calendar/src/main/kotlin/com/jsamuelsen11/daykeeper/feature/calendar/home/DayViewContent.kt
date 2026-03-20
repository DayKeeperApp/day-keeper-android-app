package com.jsamuelsen11.daykeeper.feature.calendar.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.calendar.component.CalendarEventItem
import com.jsamuelsen11.daykeeper.feature.calendar.component.EventCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// region Constants

private val ContentPadding = 16.dp
private val EventListSpacing = 8.dp
private val DateHeaderPadding = 16.dp
private const val DATE_PATTERN = "EEEE, MMMM d"

// endregion

/**
 * Day view placeholder for the calendar home screen.
 *
 * Displays a date header followed by a scrollable list of [EventCard]s for [date]. An empty state
 * is shown when [events] is empty. Full time-slot rendering is deferred to task 9.3.
 *
 * @param date The day whose events are displayed.
 * @param events Events occurring on [date], already enriched with calendar metadata.
 * @param onEventClick Called with the event ID when the user taps an event card.
 * @param modifier Optional [Modifier] applied to the root [Column].
 */
@Composable
fun DayViewContent(
  date: LocalDate,
  events: List<CalendarEventItem>,
  onEventClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val dateLabel =
    remember(date) { DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.getDefault()).format(date) }

  Column(modifier = modifier.fillMaxSize()) {
    Text(
      text = dateLabel,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(horizontal = DateHeaderPadding, vertical = DateHeaderPadding),
    )

    if (events.isEmpty()) {
      EmptyStateView(
        icon = DayKeeperIcons.Event,
        title = "No events",
        body = "Nothing scheduled for this day.",
        modifier = Modifier.fillMaxWidth(),
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
