package com.jsamuelsen11.daykeeper.feature.people.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.jsamuelsen11.daykeeper.feature.people.createedit.PersonCreateEditScreen
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
  composable<PersonCreateEditRoute> {
    PersonCreateEditScreen(onNavigateBack = { navController.popBackStack() })
  }
}
