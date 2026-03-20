package com.jsamuelsen11.daykeeper.feature.calendar.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// region Constants

private val HeaderPadding = 16.dp
private val HeaderBottomSpacing = 8.dp
private val CardSpacing = 8.dp
private val EmptyStatePadding = 16.dp
private const val DATE_PATTERN = "EEEE, MMMM d"
private const val LABEL_NO_EVENTS = "No events"
private const val PREVIEW_CREATED_AT = 1_700_000_000_000L
private const val PREVIEW_YEAR = 2026
private const val PREVIEW_MONTH = 3
private const val PREVIEW_DAY = 19
private const val PREVIEW_START_AT = 1_742_380_200_000L
private const val PREVIEW_END_AT = 1_742_383_800_000L

// endregion

/**
 * Displays a header with the formatted date and a list of [EventCard] items for that day.
 *
 * When [events] is empty a "No events" message is shown instead. Each card calls [onEventClick]
 * with the event's ID.
 *
 * @param date The selected day whose events are shown.
 * @param events The events occurring on [date], already enriched with calendar metadata.
 * @param onEventClick Called with the event ID when the user taps an event card.
 * @param modifier Modifier applied to the root [Column].
 */
@Composable
public fun DayEventsSheet(
  date: LocalDate,
  events: List<CalendarEventItem>,
  onEventClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val dateLabel =
    remember(date) { DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.getDefault()).format(date) }

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = dateLabel,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(horizontal = HeaderPadding, vertical = HeaderPadding),
    )

    Spacer(modifier = Modifier.height(HeaderBottomSpacing))

    if (events.isEmpty()) {
      Text(
        text = LABEL_NO_EVENTS,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = EmptyStatePadding),
      )
    } else {
      events.forEach { item ->
        EventCard(item = item, onClick = { onEventClick(item.event.eventId) })
        Spacer(modifier = Modifier.height(CardSpacing))
      }
    }
  }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun DayEventsSheetWithEventsPreview() {
  DayKeeperTheme {
    DayEventsSheet(
      date = LocalDate.of(PREVIEW_YEAR, PREVIEW_MONTH, PREVIEW_DAY),
      events =
        listOf(
          CalendarEventItem(
            event =
              Event(
                eventId = "e1",
                calendarId = "c1",
                spaceId = "s1",
                tenantId = "t1",
                title = "Team Standup",
                startAt = PREVIEW_START_AT,
                endAt = PREVIEW_END_AT,
                isAllDay = false,
                timezone = "UTC",
                location = "Room 3B",
                createdAt = PREVIEW_CREATED_AT,
                updatedAt = PREVIEW_CREATED_AT,
              ),
            calendarName = "Work",
            calendarColor = "#4CAF50",
            eventTypeName = "Meeting",
          ),
          CalendarEventItem(
            event =
              Event(
                eventId = "e2",
                calendarId = "c1",
                spaceId = "s1",
                tenantId = "t1",
                title = "Lunch with Alex",
                startDate = "2026-03-19",
                endDate = "2026-03-19",
                isAllDay = true,
                timezone = "UTC",
                createdAt = PREVIEW_CREATED_AT,
                updatedAt = PREVIEW_CREATED_AT,
              ),
            calendarName = "Personal",
            calendarColor = "#2196F3",
            eventTypeName = null,
          ),
        ),
      onEventClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun DayEventsSheetEmptyPreview() {
  DayKeeperTheme {
    DayEventsSheet(
      date = LocalDate.of(PREVIEW_YEAR, PREVIEW_MONTH, PREVIEW_DAY),
      events = emptyList(),
      onEventClick = {},
    )
  }
}

// endregion
