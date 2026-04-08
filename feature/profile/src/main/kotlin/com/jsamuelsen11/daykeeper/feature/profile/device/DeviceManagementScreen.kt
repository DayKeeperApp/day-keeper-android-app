package com.jsamuelsen11.daykeeper.feature.profile.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import org.koin.compose.viewmodel.koinViewModel

private val ContentPadding = 16.dp
private val CardPadding = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceManagementScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DeviceManagementViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var deleteTarget by remember { mutableStateOf<String?>(null) }

  deleteTarget?.let { deviceId ->
    ConfirmationDialog(
      title = "Remove Device",
      onConfirm = {
        viewModel.removeDevice(deviceId)
        deleteTarget = null
      },
      onDismiss = { deleteTarget = null },
      body = "This device will no longer receive sync updates.",
      confirmLabel = "Remove",
    )
  }

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = "Devices", onNavigationClick = onNavigateBack) },
  ) { padding ->
    when (val state = uiState) {
      is DeviceManagementUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(padding))
      is DeviceManagementUiState.Error ->
        Text(
          text = state.message,
          modifier = Modifier.padding(padding).padding(ContentPadding),
          color = MaterialTheme.colorScheme.error,
        )
      is DeviceManagementUiState.Success ->
        DeviceList(
          devices = state.devices,
          onRemoveDevice = { deleteTarget = it },
          modifier = Modifier.padding(padding),
        )
    }
  }
}

@Composable
private fun DeviceList(
  devices: List<DeviceItem>,
  onRemoveDevice: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(horizontal = ContentPadding),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(devices, key = { it.device.deviceId }) { item ->
      DeviceCard(item = item, onRemove = { onRemoveDevice(item.device.deviceId) })
    }
  }
}

@Composable
private fun DeviceCard(item: DeviceItem, onRemove: () -> Unit, modifier: Modifier = Modifier) {
  val containerColor =
    if (item.isCurrentDevice) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = containerColor),
  ) {
    Row(
      modifier = Modifier.padding(CardPadding).fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(imageVector = DayKeeperIcons.Devices, contentDescription = null)
          Text(text = item.device.deviceName, style = MaterialTheme.typography.bodyLarge)
        }
        if (item.isCurrentDevice) {
          Text(
            text = "This device",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
          )
        }
        Text(
          text = "Last sync: ${item.lastSyncFormatted}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      if (!item.isCurrentDevice) {
        IconButton(onClick = onRemove) {
          Icon(
            imageVector = DayKeeperIcons.Delete,
            contentDescription = "Remove device",
            tint = MaterialTheme.colorScheme.error,
          )
        }
      }
    }
  }
}
