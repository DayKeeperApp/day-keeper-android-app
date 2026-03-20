package com.jsamuelsen11.daykeeper.feature.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar as DayKeeperCalendar
import com.jsamuelsen11.daykeeper.core.model.calendar.EventType
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.calendar.createedit.ReminderEntry

private val ItemSpacing = 8.dp
private val RowIconSpacing = 12.dp
private val RowIconSize = 20.dp
private const val MIN_DESCRIPTION_LINES = 3
private const val LABEL_CALENDAR_NONE = "Select calendar"
private const val LABEL_EVENT_TYPE_NONE = "None"
private const val LABEL_ALL_DAY = "All day"
private const val LABEL_START_DATE = "Start date"
private const val LABEL_START_TIME = "Start time"
private const val LABEL_END_DATE = "End date"
private const val LABEL_END_TIME = "End time"
private const val LABEL_DOES_NOT_REPEAT = "Does not repeat"
private const val LABEL_ADD_REMINDER = "Add reminder"
private const val LABEL_REMINDERS_SECTION = "Reminders"
private const val LABEL_REMOVE_REMINDER = "Remove reminder"

@Composable
internal fun TitleField(
  title: String,
  error: String?,
  enabled: Boolean,
  onTitleChanged: (String) -> Unit,
) {
  OutlinedTextField(
    value = title,
    onValueChange = onTitleChanged,
    modifier = Modifier.fillMaxWidth(),
    label = { Text("Title") },
    isError = error != null,
    supportingText = error?.let { message -> { Text(message) } },
    singleLine = true,
    enabled = enabled,
  )
}

@Composable
internal fun DescriptionField(
  description: String,
  enabled: Boolean,
  onDescriptionChanged: (String) -> Unit,
) {
  OutlinedTextField(
    value = description,
    onValueChange = onDescriptionChanged,
    modifier = Modifier.fillMaxWidth(),
    label = { Text("Description") },
    minLines = MIN_DESCRIPTION_LINES,
    enabled = enabled,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarSelector(
  calendars: List<DayKeeperCalendar>,
  selectedCalendarId: String?,
  enabled: Boolean,
  onCalendarSelected: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedName =
    calendars.firstOrNull { it.calendarId == selectedCalendarId }?.name ?: LABEL_CALENDAR_NONE

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { if (enabled) expanded = it },
    modifier = modifier,
  ) {
    OutlinedTextField(
      value = selectedName,
      onValueChange = {},
      modifier =
        Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
      label = { Text("Calendar") },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      readOnly = true,
      enabled = enabled,
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      calendars.forEach { calendar ->
        DropdownMenuItem(
          text = { Text(calendar.name) },
          onClick = {
            onCalendarSelected(calendar.calendarId)
            expanded = false
          },
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventTypeSelector(
  eventTypes: List<EventType>,
  selectedEventTypeId: String?,
  enabled: Boolean,
  onEventTypeSelected: (String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedName =
    eventTypes.firstOrNull { it.eventTypeId == selectedEventTypeId }?.name ?: LABEL_EVENT_TYPE_NONE

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { if (enabled) expanded = it },
    modifier = modifier,
  ) {
    OutlinedTextField(
      value = selectedName,
      onValueChange = {},
      modifier =
        Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
      label = { Text("Event type") },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      readOnly = true,
      enabled = enabled,
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      DropdownMenuItem(
        text = { Text(LABEL_EVENT_TYPE_NONE) },
        onClick = {
          onEventTypeSelected(null)
          expanded = false
        },
      )
      eventTypes.forEach { eventType ->
        DropdownMenuItem(
          text = { Text(eventType.name) },
          onClick = {
            onEventTypeSelected(eventType.eventTypeId)
            expanded = false
          },
        )
      }
    }
  }
}

@Composable
internal fun AllDayToggleRow(
  isAllDay: Boolean,
  enabled: Boolean,
  onAllDayToggled: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(RowIconSpacing),
    ) {
      Icon(
        imageVector = DayKeeperIcons.Schedule,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(RowIconSize),
      )
      Text(text = LABEL_ALL_DAY, style = MaterialTheme.typography.bodyLarge)
    }
    Switch(checked = isAllDay, onCheckedChange = onAllDayToggled, enabled = enabled)
  }
}

@Composable
internal fun AllDayDateSection(
  startDate: String?,
  endDate: String?,
  dateError: String?,
  enabled: Boolean,
  onPickStartDate: () -> Unit,
  onPickEndDate: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(ItemSpacing)) {
    DatePickerRow(
      icon = DayKeeperIcons.Event,
      label = startDate ?: LABEL_START_DATE,
      enabled = enabled,
      onClick = onPickStartDate,
    )
    DatePickerRow(
      icon = DayKeeperIcons.Event,
      label = endDate ?: LABEL_END_DATE,
      enabled = enabled,
      onClick = onPickEndDate,
    )
    dateError?.let { error ->
      Text(
        text = error,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
      )
    }
  }
}

@Composable
internal fun TimedDateSection(
  startAt: Long?,
  endAt: Long?,
  timezone: String,
  dateError: String?,
  enabled: Boolean,
  onPickStartDateTime: () -> Unit,
  onPickEndDateTime: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(ItemSpacing)) {
    DatePickerRow(
      icon = DayKeeperIcons.Event,
      label = startAt?.let { formatEpochMillis(it, timezone) } ?: LABEL_START_DATE,
      enabled = enabled,
      onClick = onPickStartDateTime,
    )
    DatePickerRow(
      icon = DayKeeperIcons.Schedule,
      label = startAt?.let { formatEpochMillisTime(it, timezone) } ?: LABEL_START_TIME,
      enabled = enabled,
      onClick = onPickStartDateTime,
    )
    DatePickerRow(
      icon = DayKeeperIcons.Event,
      label = endAt?.let { formatEpochMillis(it, timezone) } ?: LABEL_END_DATE,
      enabled = enabled,
      onClick = onPickEndDateTime,
    )
    DatePickerRow(
      icon = DayKeeperIcons.Schedule,
      label = endAt?.let { formatEpochMillisTime(it, timezone) } ?: LABEL_END_TIME,
      enabled = enabled,
      onClick = onPickEndDateTime,
    )
    dateError?.let { error ->
      Text(
        text = error,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
      )
    }
  }
}

@Composable
internal fun DatePickerRow(
  icon: ImageVector,
  label: String,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RowIconSpacing),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(RowIconSize),
    )
    TextButton(onClick = onClick, enabled = enabled) { Text(label) }
  }
}

@Composable
internal fun LocationField(
  location: String,
  enabled: Boolean,
  onLocationChanged: (String) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RowIconSpacing),
  ) {
    Icon(
      imageVector = DayKeeperIcons.Location,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(RowIconSize),
    )
    OutlinedTextField(
      value = location,
      onValueChange = onLocationChanged,
      modifier = Modifier.weight(1f),
      label = { Text("Location") },
      singleLine = true,
      enabled = enabled,
    )
  }
}

@Composable
internal fun RecurrenceRow(
  recurrenceRule: RecurrenceRule?,
  enabled: Boolean,
  onPickRecurrence: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RowIconSpacing),
  ) {
    Icon(
      imageVector = DayKeeperIcons.Repeat,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(RowIconSize),
    )
    TextButton(onClick = onPickRecurrence, enabled = enabled) {
      Text(recurrenceRule?.toRruleString() ?: LABEL_DOES_NOT_REPEAT)
    }
  }
}

@Composable
internal fun RemindersSection(
  reminders: List<ReminderEntry>,
  enabled: Boolean,
  onAddReminder: () -> Unit,
  onRemoveReminder: (String) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(ItemSpacing)) {
    Text(
      text = LABEL_REMINDERS_SECTION,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    reminders.forEach { reminder ->
      ReminderEntryRow(
        entry = reminder,
        enabled = enabled,
        onRemove = { onRemoveReminder(reminder.id) },
      )
    }

    TextButton(onClick = onAddReminder, enabled = enabled) {
      Icon(
        imageVector = DayKeeperIcons.Add,
        contentDescription = null,
        modifier = Modifier.size(RowIconSize),
      )
      Text(LABEL_ADD_REMINDER)
    }
  }
}

@Composable
internal fun ReminderEntryRow(entry: ReminderEntry, enabled: Boolean, onRemove: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(RowIconSpacing),
    ) {
      Icon(
        imageVector = DayKeeperIcons.Notification,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(RowIconSize),
      )
      Text(
        text = formatReminderLabel(entry.minutesBefore),
        style = MaterialTheme.typography.bodyLarge,
      )
    }
    IconButton(onClick = onRemove, enabled = enabled) {
      Icon(
        imageVector = DayKeeperIcons.Close,
        contentDescription = LABEL_REMOVE_REMINDER,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
