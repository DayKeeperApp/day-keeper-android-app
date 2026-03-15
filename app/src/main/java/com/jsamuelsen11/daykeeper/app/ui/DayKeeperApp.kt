package com.jsamuelsen11.daykeeper.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jsamuelsen11.daykeeper.app.navigation.DayKeeperNavHost
import com.jsamuelsen11.daykeeper.app.navigation.TopLevelDestination

@Composable
fun DayKeeperApp(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  Scaffold(
    modifier = modifier.fillMaxSize(),
    bottomBar = {
      DayKeeperBottomBar(
        destinations = TopLevelDestination.entries,
        currentDestination = currentDestination,
        onNavigate = { destination ->
          navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        },
      )
    },
  ) { innerPadding ->
    DayKeeperNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
  }
}

@Composable
private fun DayKeeperBottomBar(
  destinations: List<TopLevelDestination>,
  currentDestination: NavDestination?,
  onNavigate: (TopLevelDestination) -> Unit,
  modifier: Modifier = Modifier,
) {
  NavigationBar(modifier = modifier) {
    destinations.forEach { destination ->
      val selected =
        currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true

      NavigationBarItem(
        selected = selected,
        onClick = { onNavigate(destination) },
        icon = {
          Icon(
            imageVector = destination.icon,
            contentDescription = stringResource(destination.labelResId),
          )
        },
        label = { Text(text = stringResource(destination.labelResId)) },
      )
    }
  }
}
