package com.jsamuelsen11.daykeeper.feature.profile.space.createedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.model.space.SpaceRole
import com.jsamuelsen11.daykeeper.core.model.space.SpaceType
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

private val ContentPadding = 16.dp
private val ItemSpacing = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceCreateEditScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SpaceCreateEditViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val saveComplete by viewModel.saveComplete.collectAsStateWithLifecycle()

  LaunchedEffect(saveComplete) { if (saveComplete) onNavigateBack() }

  val title =
    if ((uiState as? SpaceCreateEditUiState.Success)?.isEditMode == true) "Edit Space"
    else "Create Space"

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = title, onNavigationClick = onNavigateBack) },
  ) { padding ->
    when (val state = uiState) {
      is SpaceCreateEditUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(padding))
      is SpaceCreateEditUiState.Error ->
        Text(
          text = state.message,
          modifier = Modifier.padding(padding).padding(ContentPadding),
          color = MaterialTheme.colorScheme.error,
        )
      is SpaceCreateEditUiState.Success ->
        SpaceForm(state = state, viewModel = viewModel, modifier = Modifier.padding(padding))
    }
  }
}

@Composable
private fun SpaceForm(
  state: SpaceCreateEditUiState.Success,
  viewModel: SpaceCreateEditViewModel,
  modifier: Modifier = Modifier,
) {
  var showDeleteDialog by remember { mutableStateOf(false) }

  if (showDeleteDialog) {
    ConfirmationDialog(
      title = "Delete Space",
      onConfirm = {
        showDeleteDialog = false
        viewModel.deleteSpace()
      },
      onDismiss = { showDeleteDialog = false },
      body = "Are you sure you want to delete this space? This action cannot be undone.",
      confirmLabel = "Delete",
    )
  }

  Column(
    modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(ContentPadding)
  ) {
    OutlinedTextField(
      value = state.name,
      onValueChange = viewModel::updateName,
      label = { Text("Space Name") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(ItemSpacing))

    SpaceTypeSelector(selected = state.type, onSelected = viewModel::updateType)

    if (state.members.isNotEmpty()) {
      MemberSection(members = state.members)
    }

    Spacer(modifier = Modifier.height(ContentPadding))

    Button(
      onClick = viewModel::save,
      modifier = Modifier.fillMaxWidth(),
      enabled = state.name.isNotBlank() && !state.isSaving,
    ) {
      Text(if (state.isEditMode) "Save Changes" else "Create Space")
    }

    if (state.isEditMode) {
      Spacer(modifier = Modifier.height(ItemSpacing))
      OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Delete Space", color = MaterialTheme.colorScheme.error)
      }
    }
  }
}

@Composable
private fun MemberSection(members: List<com.jsamuelsen11.daykeeper.core.model.space.SpaceMember>) {
  Spacer(modifier = Modifier.height(ItemSpacing))
  HorizontalDivider()
  Spacer(modifier = Modifier.height(ItemSpacing))
  Text(text = "Members", style = MaterialTheme.typography.titleSmall)
  Spacer(modifier = Modifier.height(8.dp))
  members.forEach { member ->
    Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(text = member.tenantId, style = MaterialTheme.typography.bodyMedium)
      Text(
        text = memberRoleLabel(member.role),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun SpaceTypeSelector(selected: SpaceType, onSelected: (SpaceType) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  Column {
    Text(text = "Space Type", style = MaterialTheme.typography.bodyMedium)
    OutlinedTextField(
      value = spaceTypeLabel(selected),
      onValueChange = {},
      readOnly = true,
      modifier = Modifier.fillMaxWidth(),
      trailingIcon = { Text("v", modifier = Modifier.padding(end = 8.dp)) },
    )
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      SpaceType.entries.forEach { type ->
        DropdownMenuItem(
          text = { Text(spaceTypeLabel(type)) },
          onClick = {
            onSelected(type)
            expanded = false
          },
        )
      }
    }
  }
}

private fun spaceTypeLabel(type: SpaceType): String =
  when (type) {
    SpaceType.PERSONAL -> "Personal"
    SpaceType.SHARED -> "Shared"
    SpaceType.SYSTEM -> "System"
  }

private fun memberRoleLabel(role: SpaceRole): String =
  when (role) {
    SpaceRole.OWNER -> "Owner"
    SpaceRole.EDITOR -> "Editor"
    SpaceRole.VIEWER -> "Viewer"
  }
