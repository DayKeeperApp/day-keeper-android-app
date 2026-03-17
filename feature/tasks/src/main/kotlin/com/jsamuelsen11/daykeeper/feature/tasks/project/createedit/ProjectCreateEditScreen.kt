package com.jsamuelsen11.daykeeper.feature.tasks.project.createedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import org.koin.compose.viewmodel.koinViewModel

private val ContentPadding = 16.dp
private val FieldSpacing = 16.dp
private const val DESCRIPTION_MIN_LINES = 3

@Composable
fun ProjectCreateEditScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProjectCreateEditViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
      when (event) {
        ProjectCreateEditEvent.Saved -> onNavigateBack()
      }
    }
  }

  val isEditing = (uiState as? ProjectCreateEditUiState.Ready)?.isEditing ?: false

  Scaffold(
    modifier = modifier,
    topBar = {
      DayKeeperTopAppBar(
        title = if (isEditing) "Edit Project" else "New Project",
        onNavigationClick = onNavigateBack,
        actions = {
          val isSaving = (uiState as? ProjectCreateEditUiState.Ready)?.isSaving ?: false
          IconButton(onClick = viewModel::onSave, enabled = !isSaving) {
            Icon(imageVector = DayKeeperIcons.Check, contentDescription = "Save")
          }
        },
      )
    },
  ) { innerPadding ->
    when (val state = uiState) {
      is ProjectCreateEditUiState.Loading ->
        LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
      is ProjectCreateEditUiState.Ready ->
        ProjectCreateEditContent(
          state = state,
          onNameChanged = viewModel::onNameChanged,
          onDescriptionChanged = viewModel::onDescriptionChanged,
          modifier = Modifier.padding(innerPadding),
        )
    }
  }
}

@Composable
private fun ProjectCreateEditContent(
  state: ProjectCreateEditUiState.Ready,
  onNameChanged: (String) -> Unit,
  onDescriptionChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(ContentPadding),
    verticalArrangement = Arrangement.spacedBy(FieldSpacing),
  ) {
    OutlinedTextField(
      value = state.name,
      onValueChange = onNameChanged,
      label = { Text("Name") },
      isError = state.nameError != null,
      supportingText = state.nameError?.let { { Text(it) } },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
      value = state.description,
      onValueChange = onDescriptionChanged,
      label = { Text("Description (optional)") },
      minLines = DESCRIPTION_MIN_LINES,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
