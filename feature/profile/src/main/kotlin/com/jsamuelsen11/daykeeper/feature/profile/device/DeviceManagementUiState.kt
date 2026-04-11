package com.jsamuelsen11.daykeeper.feature.profile.device

import com.jsamuelsen11.daykeeper.core.model.account.Device

sealed interface DeviceManagementUiState {
  data object Loading : DeviceManagementUiState

  data class Success(val devices: List<DeviceItem>, val currentDeviceId: String?) :
    DeviceManagementUiState

  data class Error(val message: String) : DeviceManagementUiState
}

data class DeviceItem(
  val device: Device,
  val isCurrentDevice: Boolean,
  val lastSyncFormatted: String,
)
