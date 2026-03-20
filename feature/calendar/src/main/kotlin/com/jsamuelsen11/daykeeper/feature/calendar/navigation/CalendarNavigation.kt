package com.jsamuelsen11.daykeeper.feature.calendar.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.jsamuelsen11.daykeeper.feature.calendar.createedit.EventCreateEditScreen
import com.jsamuelsen11.daykeeper.feature.calendar.detail.EventDetailScreen
import com.jsamuelsen11.daykeeper.feature.calendar.home.CalendarScreen
import com.jsamuelsen11.daykeeper.feature.calendar.management.CalendarCreateEditScreen
import com.jsamuelsen11.daykeeper.feature.calendar.management.CalendarManagementScreen

fun NavGraphBuilder.calendarGraph(navController: NavHostController) {
  composable<CalendarHomeRoute> {
    CalendarScreen(
      onEventClick = { eventId -> navController.navigate(EventDetailRoute(eventId)) },
      onCreateEvent = { navController.navigate(EventCreateEditRoute()) },
      onCreateEventOnDate = { millis ->
        navController.navigate(EventCreateEditRoute(initialDateMillis = millis))
      },
      onManageCalendars = { navController.navigate(CalendarManagementRoute) },
    )
  }
  composable<EventDetailRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<EventDetailRoute>()
    EventDetailScreen(
      onNavigateBack = { navController.popBackStack() },
      onEditEvent = { navController.navigate(EventCreateEditRoute(eventId = route.eventId)) },
    )
  }
  composable<EventCreateEditRoute> {
    EventCreateEditScreen(onNavigateBack = { navController.popBackStack() })
  }
  composable<CalendarManagementRoute> {
    CalendarManagementScreen(
      onNavigateBack = { navController.popBackStack() },
      onCreateCalendar = { navController.navigate(CalendarCreateEditRoute()) },
      onEditCalendar = { calendarId -> navController.navigate(CalendarCreateEditRoute(calendarId)) },
    )
  }
  composable<CalendarCreateEditRoute> {
    CalendarCreateEditScreen(onNavigateBack = { navController.popBackStack() })
  }
}
