package com.jsamuelsen11.daykeeper.feature.people.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.jsamuelsen11.daykeeper.feature.people.detail.PersonDetailScreen
import com.jsamuelsen11.daykeeper.feature.people.list.PeopleListScreen

fun NavGraphBuilder.peopleGraph(navController: NavHostController) {
  composable<PeopleListRoute> {
    PeopleListScreen(
      onPersonClick = { personId -> navController.navigate(PersonDetailRoute(personId)) },
      onCreatePerson = { navController.navigate(PersonCreateEditRoute()) },
    )
  }
  composable<PersonDetailRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<PersonDetailRoute>()
    PersonDetailScreen(
      onNavigateBack = { navController.popBackStack() },
      onEditPerson = { navController.navigate(PersonCreateEditRoute(route.personId)) },
    )
  }
  composable<PersonCreateEditRoute> { PlaceholderScreen("Person Create/Edit") }
}

@Composable
private fun PlaceholderScreen(title: String) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = title) }
}
