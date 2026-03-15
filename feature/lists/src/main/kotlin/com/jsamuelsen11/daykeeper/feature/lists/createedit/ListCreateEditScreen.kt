package com.jsamuelsen11.daykeeper.feature.lists.createedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

private val ContentPadding = 24.dp
private val ButtonTopSpacing = 24.dp

@Composable
fun ListCreateEditScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ListCreateEditViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.events.collect { if (it is ListCreateEditEvent.Saved) onNavigateBack() }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      val title =
        when (val state = uiState) {
          is ListCreateEditUiState.Ready -> if (state.isEditing) "Edit List" else "New List"
          else -> "List"
        }
      DayKeeperTopAppBar(title = title, onNavigationClick = onNavigateBack)
    },
  ) { innerPadding ->
    when (val state = uiState) {
      is ListCreateEditUiState.Loading ->
        LoadingIndicator(modifier = Modifier.padding(innerPadding))
      is ListCreateEditUiState.Ready -> {
        Column(modifier = Modifier.padding(innerPadding).padding(ContentPadding)) {
          OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("List name") },
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
            singleLine = true,
            enabled = !state.isSaving,
          )
          Spacer(modifier = Modifier.height(ButtonTopSpacing))
          Button(
            onClick = viewModel::onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
          ) {
            Text(if (state.isEditing) "Save" else "Create")
          }
        }
      }
    }
  }
}
