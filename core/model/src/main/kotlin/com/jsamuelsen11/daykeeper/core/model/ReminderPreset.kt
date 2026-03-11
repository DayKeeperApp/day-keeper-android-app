package com.jsamuelsen11.daykeeper.core.model

/** Preset reminder times expressed as minutes before an event. */
enum class ReminderPreset(val minutesBefore: Int, val displayLabel: String) {
  AT_TIME(minutesBefore = 0, displayLabel = "At time of event"),
  MINUTES_5(minutesBefore = 5, displayLabel = "5 minutes before"),
  MINUTES_15(minutesBefore = 15, displayLabel = "15 minutes before"),
  MINUTES_30(minutesBefore = 30, displayLabel = "30 minutes before"),
  HOURS_1(minutesBefore = 60, displayLabel = "1 hour before"),
  HOURS_2(minutesBefore = 120, displayLabel = "2 hours before"),
  DAYS_1(minutesBefore = 1440, displayLabel = "1 day before"),
  DAYS_2(minutesBefore = 2880, displayLabel = "2 days before"),
  WEEKS_1(minutesBefore = 10080, displayLabel = "1 week before"),
}
