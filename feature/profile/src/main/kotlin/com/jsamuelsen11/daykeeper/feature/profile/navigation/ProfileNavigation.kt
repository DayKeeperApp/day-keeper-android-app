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
    PlaceholderScreen("Account Settings")
  }
  composable<SpaceManagementRoute> {
    PlaceholderScreen("Space Management")
  }
  composable<SpaceCreateEditRoute> {
    PlaceholderScreen("Space Create/Edit")
  }
  composable<DeviceManagementRoute> {
    PlaceholderScreen("Device Management")
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
