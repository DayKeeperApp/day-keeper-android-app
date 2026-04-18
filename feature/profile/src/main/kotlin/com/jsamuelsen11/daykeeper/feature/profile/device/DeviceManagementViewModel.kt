package com.jsamuelsen11.daykeeper.feature.profile.device

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.DeviceRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val SYNC_TIME_FORMAT = "MMM d, yyyy h:mm a"

class DeviceManagementViewModel(
  private val deviceRepository: DeviceRepository,
  private val sessionProvider: CurrentSessionProvider,
) : ViewModel() {

  private val currentDeviceName = Build.MODEL

  val uiState: StateFlow<DeviceManagementUiState> =
    deviceRepository
      .observeByTenant(sessionProvider.tenantId)
      .map<_, DeviceManagementUiState> { devices ->
        val currentId = devices.find { it.deviceName == currentDeviceName }?.deviceId
        val items =
          devices.map { device ->
            DeviceItem(
              device = device,
              isCurrentDevice = device.deviceId == currentId,
              lastSyncFormatted = formatSyncTime(device.lastSyncCursor),
            )
          }
        DeviceManagementUiState.Success(
          devices = items.sortedByDescending { it.isCurrentDevice },
          currentDeviceId = currentId,
        )
      }
      .catch { e -> emit(DeviceManagementUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        DeviceManagementUiState.Loading,
      )

  fun removeDevice(deviceId: String) {
    viewModelScope.launch { deviceRepository.delete(deviceId) }
  }

  private fun formatSyncTime(cursor: Long): String =
    if (cursor == 0L) "Never"
    else SimpleDateFormat(SYNC_TIME_FORMAT, Locale.getDefault()).format(Date(cursor))
}
