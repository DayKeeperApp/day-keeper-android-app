package com.jsamuelsen11.daykeeper.feature.profile.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

private val ContentPadding = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SyncStatusViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = "Sync Status", onNavigationClick = onNavigateBack) },
  ) { padding ->
    when (val state = uiState) {
      is SyncStatusUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(padding))
      is SyncStatusUiState.Success ->
        SyncContent(
          state = state,
          onSyncNow = viewModel::syncNow,
          modifier = Modifier.padding(padding),
        )
    }
  }
}

@Composable
private fun SyncContent(
  state: SyncStatusUiState.Success,
  onSyncNow: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize().padding(ContentPadding)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = "Status", style = MaterialTheme.typography.bodyMedium)
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (state.isSyncing) {
          CircularProgressIndicator(modifier = Modifier.height(16.dp))
        }
        Text(text = state.status, style = MaterialTheme.typography.bodyLarge)
      }
    }

    Spacer(modifier = Modifier.height(ContentPadding))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = "Last Sync", style = MaterialTheme.typography.bodyMedium)
      Text(text = state.lastSyncFormatted, style = MaterialTheme.typography.bodyLarge)
    }

    Spacer(modifier = Modifier.height(ContentPadding * 2))

    Button(onClick = onSyncNow, modifier = Modifier.fillMaxWidth(), enabled = !state.isSyncing) {
      Text("Sync Now")
    }
  }
}
