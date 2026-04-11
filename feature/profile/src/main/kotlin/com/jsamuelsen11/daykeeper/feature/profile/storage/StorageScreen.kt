package com.jsamuelsen11.daykeeper.feature.profile.storage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

private val ContentPadding = 16.dp
private const val MIN_CACHE_SIZE_MB = 50f
private const val MAX_CACHE_SIZE_MB = 500f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: StorageViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showClearDialog by remember { mutableStateOf(false) }

  if (showClearDialog) {
    ConfirmationDialog(
      title = "Clear Cache",
      onConfirm = {
        showClearDialog = false
        viewModel.clearCache()
      },
      onDismiss = { showClearDialog = false },
      body = "This will remove all cached attachments. They will be re-downloaded when needed.",
      confirmLabel = "Clear",
    )
  }

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = "Storage", onNavigationClick = onNavigateBack) },
  ) { padding ->
    when (val state = uiState) {
      is StorageUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(padding))
      is StorageUiState.Success ->
        StorageContent(
          state = state,
          onMaxCacheSizeChanged = viewModel::updateMaxCacheSize,
          onClearCache = { showClearDialog = true },
          modifier = Modifier.padding(padding),
        )
    }
  }
}

@Composable
private fun StorageContent(
  state: StorageUiState.Success,
  onMaxCacheSizeChanged: (Int) -> Unit,
  onClearCache: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize().padding(ContentPadding)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(text = "Current Cache Size", style = MaterialTheme.typography.bodyMedium)
      Text(text = "${state.currentCacheSizeMb} MB", style = MaterialTheme.typography.bodyLarge)
    }

    Spacer(modifier = Modifier.height(ContentPadding))

    Text(
      text = "Max Cache Size: ${state.maxCacheSizeMb} MB",
      style = MaterialTheme.typography.bodyMedium,
    )
    Slider(
      value = state.maxCacheSizeMb.toFloat(),
      onValueChange = { onMaxCacheSizeChanged(it.toInt()) },
      valueRange = MIN_CACHE_SIZE_MB..MAX_CACHE_SIZE_MB,
      steps = 8,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(ContentPadding))

    OutlinedButton(onClick = onClearCache, modifier = Modifier.fillMaxWidth()) {
      Text("Clear Cache")
    }
  }
}
