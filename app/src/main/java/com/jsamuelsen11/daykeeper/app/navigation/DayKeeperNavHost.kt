package com.jsamuelsen11.daykeeper.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.jsamuelsen11.daykeeper.feature.calendar.navigation.CalendarHomeRoute
import com.jsamuelsen11.daykeeper.feature.calendar.navigation.calendarGraph
import com.jsamuelsen11.daykeeper.feature.lists.navigation.ListsHomeRoute
import com.jsamuelsen11.daykeeper.feature.lists.navigation.listsGraph
import com.jsamuelsen11.daykeeper.feature.people.navigation.PeopleListRoute
import com.jsamuelsen11.daykeeper.feature.people.navigation.peopleGraph
import com.jsamuelsen11.daykeeper.feature.profile.navigation.ProfileOverviewRoute
import com.jsamuelsen11.daykeeper.feature.profile.navigation.profileGraph
import com.jsamuelsen11.daykeeper.feature.tasks.navigation.TasksHomeRoute
import com.jsamuelsen11.daykeeper.feature.tasks.navigation.tasksGraph

@Composable
fun DayKeeperNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
  NavHost(navController = navController, startDestination = CalendarRoute, modifier = modifier) {
    navigation<CalendarRoute>(startDestination = CalendarHomeRoute) { calendarGraph(navController) }
    navigation<TasksRoute>(startDestination = TasksHomeRoute) { tasksGraph(navController) }
    navigation<ListsRoute>(startDestination = ListsHomeRoute) { listsGraph(navController) }
    navigation<PeopleRoute>(startDestination = PeopleListRoute) { peopleGraph(navController) }
    navigation<ProfileRoute>(startDestination = ProfileOverviewRoute) {
      profileGraph(navController)
    }
    composable<GlobalSearchRoute> { GlobalSearchScreen() }
  }
}
