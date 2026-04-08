package com.jsamuelsen11.daykeeper.feature.profile.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.jsamuelsen11.daykeeper.feature.profile.overview.ProfileOverviewScreen
import com.jsamuelsen11.daykeeper.feature.profile.settings.AccountSettingsScreen
import com.jsamuelsen11.daykeeper.feature.profile.device.DeviceManagementScreen
import com.jsamuelsen11.daykeeper.feature.profile.space.SpaceManagementScreen
import com.jsamuelsen11.daykeeper.feature.profile.space.createedit.SpaceCreateEditScreen

fun NavGraphBuilder.profileGraph(navController: NavHostController) {
  composable<ProfileOverviewRoute> {
    ProfileOverviewScreen(
      onAccountSettingsClick = { navController.navigate(AccountSettingsRoute) },
      onSpacesClick = { navController.navigate(SpaceManagementRoute) },
      onDevicesClick = { navController.navigate(DeviceManagementRoute) },
      onSyncStatusClick = { navController.navigate(SyncStatusRoute) },
      onStorageClick = { navController.navigate(StorageRoute) },
      onAboutClick = { navController.navigate(AboutRoute) },
    )
  }
  composable<AccountSettingsRoute> {
    AccountSettingsScreen(onNavigateBack = { navController.popBackStack() })
  }
  composable<SpaceManagementRoute> {
    SpaceManagementScreen(
      onNavigateBack = { navController.popBackStack() },
      onSpaceClick = { spaceId -> navController.navigate(SpaceCreateEditRoute(spaceId)) },
      onCreateSpace = { navController.navigate(SpaceCreateEditRoute()) },
    )
  }
  composable<SpaceCreateEditRoute> {
    SpaceCreateEditScreen(onNavigateBack = { navController.popBackStack() })
  }
  composable<DeviceManagementRoute> {
    DeviceManagementScreen(onNavigateBack = { navController.popBackStack() })
  }
  composable<SyncStatusRoute> {
    PlaceholderScreen("Sync Status")
  }
  composable<StorageRoute> {
    PlaceholderScreen("Storage")
  }
  composable<AboutRoute> {
    PlaceholderScreen("About")
  }
}

@Composable
private fun PlaceholderScreen(title: String) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = title)
  }
}
