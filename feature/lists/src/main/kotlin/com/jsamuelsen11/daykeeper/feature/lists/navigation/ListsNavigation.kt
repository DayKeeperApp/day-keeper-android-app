package com.jsamuelsen11.daykeeper.feature.lists.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.jsamuelsen11.daykeeper.feature.lists.createedit.ListCreateEditScreen
import com.jsamuelsen11.daykeeper.feature.lists.detail.ShoppingListScreen
import com.jsamuelsen11.daykeeper.feature.lists.overview.ListsOverviewScreen

fun NavGraphBuilder.listsGraph(navController: NavHostController) {
  composable<ListsHomeRoute> {
    ListsOverviewScreen(
      onListClick = { listId -> navController.navigate(ListDetailRoute(listId)) },
      onCreateList = { navController.navigate(ListCreateEditRoute()) },
    )
  }
  composable<ListDetailRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<ListDetailRoute>()
    ShoppingListScreen(
      onNavigateBack = { navController.popBackStack() },
      onEditList = { navController.navigate(ListCreateEditRoute(route.listId)) },
    )
  }
  composable<ListCreateEditRoute> {
    ListCreateEditScreen(onNavigateBack = { navController.popBackStack() })
  }
}
