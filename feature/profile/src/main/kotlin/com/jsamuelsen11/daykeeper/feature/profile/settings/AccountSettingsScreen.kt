package com.jsamuelsen11.daykeeper.feature.profile.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.data.preferences.DateFormat
import com.jsamuelsen11.daykeeper.core.data.preferences.DefaultCalendarView
import com.jsamuelsen11.daykeeper.core.data.preferences.ListSortOrder
import com.jsamuelsen11.daykeeper.core.data.preferences.NotificationSound
import com.jsamuelsen11.daykeeper.core.data.preferences.ReminderLeadTime
import com.jsamuelsen11.daykeeper.core.data.preferences.ThemeMode
import com.jsamuelsen11.daykeeper.core.data.preferences.TimeFormat
import com.jsamuelsen11.daykeeper.core.data.preferences.UserPreferences
import com.jsamuelsen11.daykeeper.core.model.account.Account
import com.jsamuelsen11.daykeeper.core.model.account.WeekStart
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

private val SectionPadding = 16.dp
private val ItemSpacing = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: AccountSettingsViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = "Settings", onNavigationClick = onNavigateBack) },
  ) { padding ->
    when (val state = uiState) {
      is AccountSettingsUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(padding))
      is AccountSettingsUiState.Error ->
        Text(
          text = state.message,
          modifier = Modifier.padding(padding).padding(SectionPadding),
          color = MaterialTheme.colorScheme.error,
        )
      is AccountSettingsUiState.Success ->
        SettingsContent(
          account = state.account,
          preferences = state.preferences,
          viewModel = viewModel,
          modifier = Modifier.padding(padding),
        )
    }
  }
}

@Composable
private fun SettingsContent(
  account: Account,
  preferences: UserPreferences,
  viewModel: AccountSettingsViewModel,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(SectionPadding)
  ) {
    AccountSection(account = account, viewModel = viewModel)
    Spacer(modifier = Modifier.height(SectionPadding))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(SectionPadding))
    DisplaySection(preferences = preferences, viewModel = viewModel)
    Spacer(modifier = Modifier.height(SectionPadding))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(SectionPadding))
    NotificationSection(preferences = preferences, viewModel = viewModel)
  }
}

// region Account Settings (synced)

@Composable
private fun AccountSection(account: Account, viewModel: AccountSettingsViewModel) {
  SectionHeader(title = "Account")

  var displayName by remember(account.displayName) { mutableStateOf(account.displayName) }
  OutlinedTextField(
    value = displayName,
    onValueChange = {
      displayName = it
      viewModel.updateDisplayName(it)
    },
    label = { Text("Display Name") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
  )

  Spacer(modifier = Modifier.height(ItemSpacing))

  TimezoneDropdown(selected = account.timezone, onTimezoneSelected = viewModel::updateTimezone)

  Spacer(modifier = Modifier.height(ItemSpacing))

  Text(text = "Week starts on", style = MaterialTheme.typography.bodyMedium)
  WeekStart.entries.forEach { option ->
    Row(verticalAlignment = Alignment.CenterVertically) {
      RadioButton(
        selected = account.weekStart == option,
        onClick = { viewModel.updateWeekStart(option) },
      )
      Text(
        text = option.name.lowercase().replaceFirstChar { it.uppercase() },
        modifier = Modifier.clickable { viewModel.updateWeekStart(option) },
      )
    }
  }
}

@Composable
private fun TimezoneDropdown(selected: String, onTimezoneSelected: (String) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  var query by remember { mutableStateOf("") }
  val allZones = remember { java.time.ZoneId.getAvailableZoneIds().sorted() }
  val filtered =
    remember(query) {
      if (query.isBlank()) allZones else allZones.filter { it.contains(query, ignoreCase = true) }
    }

  Column {
    OutlinedTextField(
      value = if (expanded) query else selected,
      onValueChange = {
        query = it
        expanded = true
      },
      label = { Text("Timezone") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().clickable { expanded = true },
    )
    DropdownMenu(
      expanded = expanded && filtered.isNotEmpty(),
      onDismissRequest = {
        expanded = false
        query = ""
      },
    ) {
      filtered.take(TIMEZONE_DROPDOWN_MAX_ITEMS).forEach { zone ->
        DropdownMenuItem(
          text = { Text(zone) },
          onClick = {
            onTimezoneSelected(zone)
            expanded = false
            query = ""
          },
        )
      }
    }
  }
}

// endregion

// region Display & UX Preferences (device-local)

@Composable
private fun DisplaySection(preferences: UserPreferences, viewModel: AccountSettingsViewModel) {
  SectionHeader(title = "Display & UX")

  EnumSelector(
    label = "Theme",
    selected = preferences.themeMode,
    entries = ThemeMode.entries,
    onSelected = viewModel::updateThemeMode,
    displayName = Labels::themeMode,
  )

  EnumSelector(
    label = "Default Calendar View",
    selected = preferences.defaultCalendarView,
    entries = DefaultCalendarView.entries,
    onSelected = viewModel::updateDefaultCalendarView,
    displayName = Labels::calendarView,
  )

  EnumSelector(
    label = "Date Format",
    selected = preferences.dateFormat,
    entries = DateFormat.entries,
    onSelected = viewModel::updateDateFormat,
    displayName = Labels::dateFormat,
  )

  EnumSelector(
    label = "Time Format",
    selected = preferences.timeFormat,
    entries = TimeFormat.entries,
    onSelected = viewModel::updateTimeFormat,
    displayName = Labels::timeFormat,
  )

  EnumSelector(
    label = "List Sort Order",
    selected = preferences.listSortOrder,
    entries = ListSortOrder.entries,
    onSelected = viewModel::updateListSortOrder,
    displayName = Labels::sortOrder,
  )

  SwitchRow(
    label = "Compact Mode",
    checked = preferences.compactMode,
    onCheckedChange = viewModel::updateCompactMode,
  )
}

// endregion

// region Notification Preferences (device-local)

@Composable
private fun NotificationSection(preferences: UserPreferences, viewModel: AccountSettingsViewModel) {
  SectionHeader(title = "Notifications")

  SwitchRow(
    label = "Do Not Disturb",
    checked = preferences.dndEnabled,
    onCheckedChange = viewModel::updateDndEnabled,
  )

  if (preferences.dndEnabled) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(ItemSpacing),
    ) {
      OutlinedTextField(
        value = preferences.dndStartTime,
        onValueChange = viewModel::updateDndStartTime,
        label = { Text("Start") },
        singleLine = true,
        modifier = Modifier.weight(1f),
      )
      OutlinedTextField(
        value = preferences.dndEndTime,
        onValueChange = viewModel::updateDndEndTime,
        label = { Text("End") },
        singleLine = true,
        modifier = Modifier.weight(1f),
      )
    }
    Spacer(modifier = Modifier.height(ItemSpacing))
  }

  EnumSelector(
    label = "Default Reminder",
    selected = preferences.defaultReminderLeadTime,
    entries = ReminderLeadTime.entries,
    onSelected = viewModel::updateDefaultReminderLeadTime,
    displayName = Labels::reminderLeadTime,
  )

  EnumSelector(
    label = "Notification Sound",
    selected = preferences.notificationSound,
    entries = NotificationSound.entries,
    onSelected = viewModel::updateNotificationSound,
    displayName = Labels::notificationSound,
  )

  SwitchRow("Events", preferences.notifyEvents, viewModel::updateNotifyEvents)
  SwitchRow("Tasks", preferences.notifyTasks, viewModel::updateNotifyTasks)
  SwitchRow("Lists", preferences.notifyLists, viewModel::updateNotifyLists)
  SwitchRow("People", preferences.notifyPeople, viewModel::updateNotifyPeople)
}

// endregion

// region Shared components

@Composable
private fun SectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.primary,
  )
  Spacer(modifier = Modifier.height(ItemSpacing))
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyLarge)
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

@Composable
private fun <T : Enum<T>> EnumSelector(
  label: String,
  selected: T,
  entries: List<T>,
  onSelected: (T) -> Unit,
  displayName: (T) -> String,
) {
  var expanded by remember { mutableStateOf(false) }
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium)
    Text(
      text = displayName(selected),
      modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(vertical = 8.dp),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.primary,
    )
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      entries.forEach { entry ->
        DropdownMenuItem(
          text = { Text(displayName(entry)) },
          onClick = {
            onSelected(entry)
            expanded = false
          },
        )
      }
    }
  }
}

// endregion

// region Display labels

private const val TIMEZONE_DROPDOWN_MAX_ITEMS = 10

private object Labels {
  fun themeMode(mode: ThemeMode): String =
    when (mode) {
      ThemeMode.SYSTEM -> "System Default"
      ThemeMode.LIGHT -> "Light"
      ThemeMode.DARK -> "Dark"
    }

  fun calendarView(view: DefaultCalendarView): String =
    when (view) {
      DefaultCalendarView.MONTH -> "Month"
      DefaultCalendarView.WEEK -> "Week"
      DefaultCalendarView.DAY -> "Day"
    }

  fun dateFormat(format: DateFormat): String =
    when (format) {
      DateFormat.SYSTEM -> "System Default"
      DateFormat.MM_DD_YYYY -> "MM/DD/YYYY"
      DateFormat.DD_MM_YYYY -> "DD/MM/YYYY"
    }

  fun timeFormat(format: TimeFormat): String =
    when (format) {
      TimeFormat.TWELVE_HOUR -> "12-hour"
      TimeFormat.TWENTY_FOUR_HOUR -> "24-hour"
      TimeFormat.SYSTEM -> "System Default"
    }

  fun sortOrder(order: ListSortOrder): String =
    when (order) {
      ListSortOrder.MANUAL -> "Manual"
      ListSortOrder.ALPHABETICAL -> "Alphabetical"
      ListSortOrder.DATE_ADDED -> "Date Added"
    }

  fun reminderLeadTime(time: ReminderLeadTime): String =
    when (time) {
      ReminderLeadTime.NONE -> "None"
      ReminderLeadTime.FIVE_MINUTES -> "5 minutes"
      ReminderLeadTime.FIFTEEN_MINUTES -> "15 minutes"
      ReminderLeadTime.THIRTY_MINUTES -> "30 minutes"
      ReminderLeadTime.ONE_HOUR -> "1 hour"
      ReminderLeadTime.ONE_DAY -> "1 day"
    }

  fun notificationSound(sound: NotificationSound): String =
    when (sound) {
      NotificationSound.DEFAULT -> "Default"
      NotificationSound.SILENT -> "Silent"
      NotificationSound.CUSTOM -> "Custom"
    }
}

// endregion
