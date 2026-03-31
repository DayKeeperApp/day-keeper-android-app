package com.jsamuelsen11.daykeeper.core.data.preferences

import kotlinx.coroutines.flow.Flow

public interface UserPreferencesRepository {

  public val userPreferences: Flow<UserPreferences>

  public suspend fun setThemeMode(themeMode: ThemeMode)

  public suspend fun setDefaultCalendarView(view: DefaultCalendarView)

  public suspend fun setDateFormat(format: DateFormat)

  public suspend fun setTimeFormat(format: TimeFormat)

  public suspend fun setListSortOrder(order: ListSortOrder)

  public suspend fun setCompactMode(enabled: Boolean)

  public suspend fun setDndEnabled(enabled: Boolean)

  public suspend fun setDndStartTime(time: String)

  public suspend fun setDndEndTime(time: String)

  public suspend fun setDefaultReminderLeadTime(leadTime: ReminderLeadTime)

  public suspend fun setNotificationSound(sound: NotificationSound)

  public suspend fun setNotifyEvents(enabled: Boolean)

  public suspend fun setNotifyTasks(enabled: Boolean)

  public suspend fun setNotifyLists(enabled: Boolean)

  public suspend fun setNotifyPeople(enabled: Boolean)

  public suspend fun setAttachmentCacheSizeMb(sizeMb: Int)
}
