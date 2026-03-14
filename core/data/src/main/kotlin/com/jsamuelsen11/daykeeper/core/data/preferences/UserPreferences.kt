package com.jsamuelsen11.daykeeper.core.data.preferences

public data class UserPreferences(
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val defaultCalendarView: DefaultCalendarView = DefaultCalendarView.MONTH,
  val dateFormat: DateFormat = DateFormat.SYSTEM,
  val timeFormat: TimeFormat = TimeFormat.SYSTEM,
  val listSortOrder: ListSortOrder = ListSortOrder.MANUAL,
  val compactMode: Boolean = false,
  val dndEnabled: Boolean = false,
  val dndStartTime: String = "22:00",
  val dndEndTime: String = "07:00",
  val defaultReminderLeadTime: ReminderLeadTime = ReminderLeadTime.FIFTEEN_MINUTES,
  val notificationSound: NotificationSound = NotificationSound.DEFAULT,
  val notifyEvents: Boolean = true,
  val notifyTasks: Boolean = true,
  val notifyLists: Boolean = true,
  val notifyPeople: Boolean = false,
)
