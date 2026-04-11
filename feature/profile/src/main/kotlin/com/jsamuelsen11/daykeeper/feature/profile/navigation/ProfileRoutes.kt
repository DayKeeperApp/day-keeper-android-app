package com.jsamuelsen11.daykeeper.feature.profile.navigation

import kotlinx.serialization.Serializable

@Serializable object ProfileOverviewRoute

@Serializable object AccountSettingsRoute

@Serializable object SpaceManagementRoute

@Serializable data class SpaceCreateEditRoute(val spaceId: String? = null)

@Serializable object DeviceManagementRoute

@Serializable object SyncStatusRoute

@Serializable object StorageRoute

@Serializable object AboutRoute
