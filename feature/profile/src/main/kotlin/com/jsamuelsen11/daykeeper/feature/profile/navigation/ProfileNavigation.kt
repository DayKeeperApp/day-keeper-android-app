package com.jsamuelsen11.daykeeper.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.jsamuelsen11.daykeeper.feature.profile.about.AboutScreen
import com.jsamuelsen11.daykeeper.feature.profile.device.DeviceManagementScreen
import com.jsamuelsen11.daykeeper.feature.profile.overview.ProfileOverviewScreen
import com.jsamuelsen11.daykeeper.feature.profile.settings.AccountSettingsScreen
import com.jsamuelsen11.daykeeper.feature.profile.space.SpaceManagementScreen
import com.jsamuelsen11.daykeeper.feature.profile.space.createedit.SpaceCreateEditScreen
import com.jsamuelsen11.daykeeper.feature.profile.storage.StorageScreen
import com.jsamuelsen11.daykeeper.feature.profile.sync.SyncStatusScreen

fun NavGraphBuilder.profileGraph(
  navController: NavHostController,
  onOpenLicenses: () -> Unit = {},
) {
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
    SyncStatusScreen(onNavigateBack = { navController.popBackStack() })
  }
  composable<StorageRoute> { StorageScreen(onNavigateBack = { navController.popBackStack() }) }
  composable<AboutRoute> {
    AboutScreen(onNavigateBack = { navController.popBackStack() }, onOpenLicenses = onOpenLicenses)
  }
}
