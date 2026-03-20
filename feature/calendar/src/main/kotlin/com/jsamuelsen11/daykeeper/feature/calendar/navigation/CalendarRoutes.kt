package com.jsamuelsen11.daykeeper.feature.calendar.navigation

import kotlinx.serialization.Serializable

@Serializable object CalendarHomeRoute

@Serializable data class EventDetailRoute(val eventId: String)

@Serializable
data class EventCreateEditRoute(
  val eventId: String? = null,
  val calendarId: String? = null,
  val initialDateMillis: Long? = null,
)

@Serializable object CalendarManagementRoute

@Serializable data class CalendarCreateEditRoute(val calendarId: String? = null)
