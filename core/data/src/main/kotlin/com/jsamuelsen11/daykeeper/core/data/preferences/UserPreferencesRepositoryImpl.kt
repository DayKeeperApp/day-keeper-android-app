package com.jsamuelsen11.daykeeper.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class UserPreferencesRepositoryImpl(private val dataStore: DataStore<Preferences>) :
  UserPreferencesRepository {

  override val userPreferences: Flow<UserPreferences> =
    dataStore.data.map { prefs -> prefs.toUserPreferences() }

  override suspend fun setThemeMode(themeMode: ThemeMode) {
    dataStore.edit { it[KEY_THEME_MODE] = themeMode.name }
  }

  override suspend fun setDefaultCalendarView(view: DefaultCalendarView) {
    dataStore.edit { it[KEY_DEFAULT_CALENDAR_VIEW] = view.name }
  }

  override suspend fun setDateFormat(format: DateFormat) {
    dataStore.edit { it[KEY_DATE_FORMAT] = format.name }
  }

  override suspend fun setTimeFormat(format: TimeFormat) {
    dataStore.edit { it[KEY_TIME_FORMAT] = format.name }
  }

  override suspend fun setListSortOrder(order: ListSortOrder) {
    dataStore.edit { it[KEY_LIST_SORT_ORDER] = order.name }
  }

  override suspend fun setCompactMode(enabled: Boolean) {
    dataStore.edit { it[KEY_COMPACT_MODE] = enabled }
  }

  override suspend fun setDndEnabled(enabled: Boolean) {
    dataStore.edit { it[KEY_DND_ENABLED] = enabled }
  }

  override suspend fun setDndStartTime(time: String) {
    dataStore.edit { it[KEY_DND_START_TIME] = time }
  }

  override suspend fun setDndEndTime(time: String) {
    dataStore.edit { it[KEY_DND_END_TIME] = time }
  }

  override suspend fun setDefaultReminderLeadTime(leadTime: ReminderLeadTime) {
    dataStore.edit { it[KEY_DEFAULT_REMINDER_LEAD_TIME] = leadTime.name }
  }

  override suspend fun setNotificationSound(sound: NotificationSound) {
    dataStore.edit { it[KEY_NOTIFICATION_SOUND] = sound.name }
  }

  override suspend fun setNotifyEvents(enabled: Boolean) {
    dataStore.edit { it[KEY_NOTIFY_EVENTS] = enabled }
  }

  override suspend fun setNotifyTasks(enabled: Boolean) {
    dataStore.edit { it[KEY_NOTIFY_TASKS] = enabled }
  }

  override suspend fun setNotifyLists(enabled: Boolean) {
    dataStore.edit { it[KEY_NOTIFY_LISTS] = enabled }
  }

  override suspend fun setNotifyPeople(enabled: Boolean) {
    dataStore.edit { it[KEY_NOTIFY_PEOPLE] = enabled }
  }
}

private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
private val KEY_DEFAULT_CALENDAR_VIEW = stringPreferencesKey("default_calendar_view")
private val KEY_DATE_FORMAT = stringPreferencesKey("date_format")
private val KEY_TIME_FORMAT = stringPreferencesKey("time_format")
private val KEY_LIST_SORT_ORDER = stringPreferencesKey("list_sort_order")
private val KEY_COMPACT_MODE = booleanPreferencesKey("compact_mode")
private val KEY_DND_ENABLED = booleanPreferencesKey("dnd_enabled")
private val KEY_DND_START_TIME = stringPreferencesKey("dnd_start_time")
private val KEY_DND_END_TIME = stringPreferencesKey("dnd_end_time")
private val KEY_DEFAULT_REMINDER_LEAD_TIME = stringPreferencesKey("default_reminder_lead_time")
private val KEY_NOTIFICATION_SOUND = stringPreferencesKey("notification_sound")
private val KEY_NOTIFY_EVENTS = booleanPreferencesKey("notify_events")
private val KEY_NOTIFY_TASKS = booleanPreferencesKey("notify_tasks")
private val KEY_NOTIFY_LISTS = booleanPreferencesKey("notify_lists")
private val KEY_NOTIFY_PEOPLE = booleanPreferencesKey("notify_people")

private fun Preferences.toUserPreferences(): UserPreferences {
  val defaults = UserPreferences()
  return UserPreferences(
    themeMode = enumValue(KEY_THEME_MODE, defaults.themeMode),
    defaultCalendarView = enumValue(KEY_DEFAULT_CALENDAR_VIEW, defaults.defaultCalendarView),
    dateFormat = enumValue(KEY_DATE_FORMAT, defaults.dateFormat),
    timeFormat = enumValue(KEY_TIME_FORMAT, defaults.timeFormat),
    listSortOrder = enumValue(KEY_LIST_SORT_ORDER, defaults.listSortOrder),
    compactMode = this[KEY_COMPACT_MODE] ?: defaults.compactMode,
    dndEnabled = this[KEY_DND_ENABLED] ?: defaults.dndEnabled,
    dndStartTime = this[KEY_DND_START_TIME] ?: defaults.dndStartTime,
    dndEndTime = this[KEY_DND_END_TIME] ?: defaults.dndEndTime,
    defaultReminderLeadTime =
      enumValue(KEY_DEFAULT_REMINDER_LEAD_TIME, defaults.defaultReminderLeadTime),
    notificationSound = enumValue(KEY_NOTIFICATION_SOUND, defaults.notificationSound),
    notifyEvents = this[KEY_NOTIFY_EVENTS] ?: defaults.notifyEvents,
    notifyTasks = this[KEY_NOTIFY_TASKS] ?: defaults.notifyTasks,
    notifyLists = this[KEY_NOTIFY_LISTS] ?: defaults.notifyLists,
    notifyPeople = this[KEY_NOTIFY_PEOPLE] ?: defaults.notifyPeople,
  )
}

private inline fun <reified T : Enum<T>> Preferences.enumValue(
  key: Preferences.Key<String>,
  default: T,
): T {
  val name = this[key] ?: return default
  return try {
    enumValueOf<T>(name)
  } catch (_: IllegalArgumentException) {
    default
  }
}
