package com.jsamuelsen11.daykeeper.feature.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceFrequency
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// region Constants

private val ColorBarWidth = 4.dp
private val CardHorizontalPadding = 4.dp
private val CardContentPadding = 12.dp
private val MetaRowTopPadding = 4.dp
private val MetaIconSize = 14.dp
private val MetaIconSpacing = 4.dp
private val MetaRowSpacing = 2.dp
private const val TIME_PATTERN = "h:mm a"
private const val LABEL_ALL_DAY = "All day"
private const val PREVIEW_CREATED_AT = 1_700_000_000_000L
private const val PREVIEW_START_AT = 1_742_380_200_000L
private const val PREVIEW_END_AT = 1_742_383_800_000L

// endregion

/**
 * A card that displays a single calendar event in a list.
 *
 * A [ColorBarWidth]-wide color bar on the leading edge uses the parent calendar's hex color. The
 * card body shows the event title (single line, ellipsis overflow), a formatted time range or "All
 * day", an optional location row, and an optional recurrence icon when the event repeats.
 *
 * @param item The enriched event item to display.
 * @param onClick Called when the user taps the card.
 * @param modifier Modifier applied to the outer [Card].
 */
@Composable
public fun EventCard(item: CalendarEventItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val calendarColor = remember(item.calendarColor) { item.calendarColor.parseHexColor() }
  val timeLabel = remember(item.event) { item.event.formatTimeLabel() }

  Card(modifier = modifier.fillMaxWidth().padding(horizontal = CardHorizontalPadding)) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).clickable(onClick = onClick)) {
      Box(
        modifier =
          Modifier.width(ColorBarWidth)
            .fillMaxHeight()
            .background(calendarColor ?: MaterialTheme.colorScheme.primary)
      )

      Column(modifier = Modifier.weight(1f).padding(CardContentPadding)) {
        Text(
          text = item.event.title,
          style = MaterialTheme.typography.bodyLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(MetaRowTopPadding))

        Text(
          text = timeLabel,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        item.event.location?.let { location ->
          Spacer(modifier = Modifier.height(MetaRowSpacing))
          LocationRow(location = location)
        }

        if (item.event.recurrenceRule != null) {
          Spacer(modifier = Modifier.height(MetaRowSpacing))
          RecurrenceIndicator()
        }
      }
    }
  }
}

@Composable
private fun LocationRow(location: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = DayKeeperIcons.Location,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(MetaIconSize),
    )
    Spacer(modifier = Modifier.width(MetaIconSpacing))
    Text(
      text = location,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun RecurrenceIndicator() {
  Icon(
    imageVector = DayKeeperIcons.Repeat,
    contentDescription = "Recurring event",
    tint = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.size(MetaIconSize),
  )
}

private fun Event.formatTimeLabel(): String {
  val startMillis = if (isAllDay) null else startAt
  if (startMillis == null) return LABEL_ALL_DAY
  val formatter =
    DateTimeFormatter.ofPattern(TIME_PATTERN, Locale.getDefault()).withZone(ZoneId.of(timezone))
  val start = formatter.format(Instant.ofEpochMilli(startMillis))
  val endMillis = endAt
  return if (endMillis != null) {
    "$start - ${formatter.format(Instant.ofEpochMilli(endMillis))}"
  } else {
    start
  }
}

private fun String.parseHexColor(): Color? =
  try {
    Color(this.toColorInt())
  } catch (_: IllegalArgumentException) {
    null
  }

// region Previews

@Preview(showBackground = true)
@Composable
private fun EventCardTimedPreview() {
  DayKeeperTheme {
    EventCard(
      item =
        CalendarEventItem(
          event =
            Event(
              eventId = "e1",
              calendarId = "c1",
              spaceId = "s1",
              tenantId = "t1",
              title = "Team Standup",
              description = "Daily sync",
              startAt = PREVIEW_START_AT,
              endAt = PREVIEW_END_AT,
              isAllDay = false,
              timezone = "UTC",
              location = "Room 3B",
              recurrenceRule = null,
              createdAt = PREVIEW_CREATED_AT,
              updatedAt = PREVIEW_CREATED_AT,
            ),
          calendarName = "Work",
          calendarColor = "#4CAF50",
          eventTypeName = "Meeting",
        ),
      onClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun EventCardAllDayRecurringPreview() {
  DayKeeperTheme {
    EventCard(
      item =
        CalendarEventItem(
          event =
            Event(
              eventId = "e2",
              calendarId = "c1",
              spaceId = "s1",
              tenantId = "t1",
              title = "Company All-Hands",
              startDate = "2026-03-19",
              endDate = "2026-03-19",
              isAllDay = true,
              timezone = "UTC",
              recurrenceRule = RecurrenceRule(frequency = RecurrenceFrequency.WEEKLY),
              createdAt = PREVIEW_CREATED_AT,
              updatedAt = PREVIEW_CREATED_AT,
            ),
          calendarName = "Work",
          calendarColor = "#2196F3",
          eventTypeName = null,
        ),
      onClick = {},
    )
  }
}

// endregion
