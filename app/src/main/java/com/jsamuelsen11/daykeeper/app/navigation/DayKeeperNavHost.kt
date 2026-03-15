package com.jsamuelsen11.daykeeper.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.jsamuelsen11.daykeeper.feature.lists.navigation.ListsHomeRoute
import com.jsamuelsen11.daykeeper.feature.lists.navigation.listsGraph
import com.jsamuelsen11.daykeeper.feature.people.navigation.PeopleListRoute
import com.jsamuelsen11.daykeeper.feature.people.navigation.peopleGraph

@Composable
fun DayKeeperNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
  NavHost(navController = navController, startDestination = CalendarRoute, modifier = modifier) {
    navigation<CalendarRoute>(startDestination = CalendarHomeRoute) {
      composable<CalendarHomeRoute> { ComingSoonScreen(TopLevelDestination.CALENDAR) }
    }
    navigation<TasksRoute>(startDestination = TasksHomeRoute) {
      composable<TasksHomeRoute> { ComingSoonScreen(TopLevelDestination.TASKS) }
    }
    navigation<ListsRoute>(startDestination = ListsHomeRoute) { listsGraph(navController) }
    navigation<PeopleRoute>(startDestination = PeopleListRoute) { peopleGraph() }
    navigation<ProfileRoute>(startDestination = ProfileHomeRoute) {
      composable<ProfileHomeRoute> { ComingSoonScreen(TopLevelDestination.PROFILE) }
    }
    composable<GlobalSearchRoute> { GlobalSearchScreen() }
  }
}
