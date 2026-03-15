package com.jsamuelsen11.daykeeper.feature.people.createedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethodType
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import org.koin.compose.viewmodel.koinViewModel

private val SectionSpacing = 16.dp
private val FieldSpacing = 8.dp

@Composable
fun PersonCreateEditScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PersonCreateEditViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.events.collect { if (it is PersonCreateEditEvent.Saved) onNavigateBack() }
  }

  val title =
    when (val state = uiState) {
      is PersonCreateEditUiState.Ready -> if (state.isEditing) "Edit Person" else "New Person"
      else -> "Person"
    }

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = title, onNavigationClick = onNavigateBack) },
  ) { innerPadding ->
    when (val state = uiState) {
      is PersonCreateEditUiState.Loading ->
        LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
      is PersonCreateEditUiState.Ready ->
        PersonFormContent(
          state = state,
          viewModel = viewModel,
          modifier = Modifier.padding(innerPadding),
        )
    }
  }
}

@Composable
private fun PersonFormContent(
  state: PersonCreateEditUiState.Ready,
  viewModel: PersonCreateEditViewModel,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(SectionSpacing)
  ) {
    NameSection(
      firstName = state.firstName,
      lastName = state.lastName,
      nickname = state.nickname,
      firstNameError = state.firstNameError,
      lastNameError = state.lastNameError,
      onFirstNameChanged = viewModel::onFirstNameChanged,
      onLastNameChanged = viewModel::onLastNameChanged,
      onNicknameChanged = viewModel::onNicknameChanged,
    )

    Spacer(modifier = Modifier.height(SectionSpacing))
    OutlinedTextField(
      value = state.notes,
      onValueChange = viewModel::onNotesChanged,
      label = { Text("Notes") },
      modifier = Modifier.fillMaxWidth(),
      minLines = 2,
    )

    Spacer(modifier = Modifier.height(SectionSpacing))
    HorizontalDivider()

    ContactMethodsSection(
      entries = state.contactMethods,
      onAdd = viewModel::addContactMethod,
      onRemove = viewModel::removeContactMethod,
      onUpdate = viewModel::updateContactMethod,
    )

    HorizontalDivider()

    AddressesSection(
      entries = state.addresses,
      onAdd = viewModel::addAddress,
      onRemove = viewModel::removeAddress,
      onUpdate = viewModel::updateAddress,
    )

    HorizontalDivider()

    ImportantDatesSection(
      entries = state.importantDates,
      onAdd = viewModel::addImportantDate,
      onRemove = viewModel::removeImportantDate,
      onUpdate = viewModel::updateImportantDate,
    )

    Spacer(modifier = Modifier.height(SectionSpacing))
    Button(
      onClick = viewModel::onSave,
      modifier = Modifier.fillMaxWidth(),
      enabled = !state.isSaving,
    ) {
      Text(if (state.isEditing) "Save" else "Create")
    }
  }
}

@Composable
private fun NameSection(
  firstName: String,
  lastName: String,
  nickname: String,
  firstNameError: String?,
  lastNameError: String?,
  onFirstNameChanged: (String) -> Unit,
  onLastNameChanged: (String) -> Unit,
  onNicknameChanged: (String) -> Unit,
) {
  OutlinedTextField(
    value = firstName,
    onValueChange = onFirstNameChanged,
    label = { Text("First name *") },
    isError = firstNameError != null,
    supportingText = firstNameError?.let { { Text(it) } },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
  )
  Spacer(modifier = Modifier.height(FieldSpacing))
  OutlinedTextField(
    value = lastName,
    onValueChange = onLastNameChanged,
    label = { Text("Last name *") },
    isError = lastNameError != null,
    supportingText = lastNameError?.let { { Text(it) } },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
  )
  Spacer(modifier = Modifier.height(FieldSpacing))
  OutlinedTextField(
    value = nickname,
    onValueChange = onNicknameChanged,
    label = { Text("Nickname") },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
  )
}

@Composable
private fun ContactMethodsSection(
  entries: List<ContactMethodFormEntry>,
  onAdd: () -> Unit,
  onRemove: (String) -> Unit,
  onUpdate: (String, ContactMethodFormEntry) -> Unit,
) {
  FormSectionHeader(title = "Contact Methods", onAdd = onAdd)
  entries.forEach { entry ->
    ContactMethodFormRow(
      entry = entry,
      onRemove = { onRemove(entry.tempId) },
      onUpdate = { onUpdate(entry.tempId, it) },
    )
    Spacer(modifier = Modifier.height(FieldSpacing))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactMethodFormRow(
  entry: ContactMethodFormEntry,
  onRemove: () -> Unit,
  onUpdate: (ContactMethodFormEntry) -> Unit,
) {
  var typeExpanded by remember { mutableStateOf(false) }

  Row(verticalAlignment = Alignment.Top) {
    Column(modifier = Modifier.weight(1f)) {
      ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
        OutlinedTextField(
          value = entry.type.name,
          onValueChange = {},
          readOnly = true,
          label = { Text("Type") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
          modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
          singleLine = true,
        )
        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
          ContactMethodType.entries.forEach { type ->
            DropdownMenuItem(
              text = { Text(type.name) },
              onClick = {
                onUpdate(entry.copy(type = type))
                typeExpanded = false
              },
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(FieldSpacing))
      OutlinedTextField(
        value = entry.value,
        onValueChange = { onUpdate(entry.copy(value = it)) },
        label = { Text("Value") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )
      Spacer(modifier = Modifier.height(FieldSpacing))
      OutlinedTextField(
        value = entry.label,
        onValueChange = { onUpdate(entry.copy(label = it)) },
        label = { Text("Label (e.g. Home, Work)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )
    }
    RemoveButton(onClick = onRemove)
  }
}

@Composable
private fun AddressesSection(
  entries: List<AddressFormEntry>,
  onAdd: () -> Unit,
  onRemove: (String) -> Unit,
  onUpdate: (String, AddressFormEntry) -> Unit,
) {
  FormSectionHeader(title = "Addresses", onAdd = onAdd)
  entries.forEach { entry ->
    Row(verticalAlignment = Alignment.Top) {
      Column(modifier = Modifier.weight(1f)) {
        AddressFormFields(entry = entry, onUpdate = { onUpdate(entry.tempId, it) })
      }
      RemoveButton(onClick = { onRemove(entry.tempId) })
    }
    Spacer(modifier = Modifier.height(FieldSpacing))
  }
}

@Composable
private fun AddressFormFields(entry: AddressFormEntry, onUpdate: (AddressFormEntry) -> Unit) {
  OutlinedTextField(
    value = entry.label,
    onValueChange = { onUpdate(entry.copy(label = it)) },
    label = { Text("Label (e.g. Home, Work)") },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
  )
  Spacer(modifier = Modifier.height(FieldSpacing))
  OutlinedTextField(
    value = entry.street,
    onValueChange = { onUpdate(entry.copy(street = it)) },
    label = { Text("Street") },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
  )
  Spacer(modifier = Modifier.height(FieldSpacing))
  Row {
    OutlinedTextField(
      value = entry.city,
      onValueChange = { onUpdate(entry.copy(city = it)) },
      label = { Text("City") },
      modifier = Modifier.weight(1f),
      singleLine = true,
    )
    Spacer(modifier = Modifier.width(FieldSpacing))
    OutlinedTextField(
      value = entry.state,
      onValueChange = { onUpdate(entry.copy(state = it)) },
      label = { Text("State") },
      modifier = Modifier.weight(1f),
      singleLine = true,
    )
  }
  Spacer(modifier = Modifier.height(FieldSpacing))
  Row {
    OutlinedTextField(
      value = entry.postalCode,
      onValueChange = { onUpdate(entry.copy(postalCode = it)) },
      label = { Text("Postal code") },
      modifier = Modifier.weight(1f),
      singleLine = true,
    )
    Spacer(modifier = Modifier.width(FieldSpacing))
    OutlinedTextField(
      value = entry.country,
      onValueChange = { onUpdate(entry.copy(country = it)) },
      label = { Text("Country") },
      modifier = Modifier.weight(1f),
      singleLine = true,
    )
  }
}

@Composable
private fun ImportantDatesSection(
  entries: List<ImportantDateFormEntry>,
  onAdd: () -> Unit,
  onRemove: (String) -> Unit,
  onUpdate: (String, ImportantDateFormEntry) -> Unit,
) {
  FormSectionHeader(title = "Important Dates", onAdd = onAdd)
  entries.forEach { entry ->
    ImportantDateFormRow(
      entry = entry,
      onRemove = { onRemove(entry.tempId) },
      onUpdate = { onUpdate(entry.tempId, it) },
    )
    Spacer(modifier = Modifier.height(FieldSpacing))
  }
}

@Composable
private fun ImportantDateFormRow(
  entry: ImportantDateFormEntry,
  onRemove: () -> Unit,
  onUpdate: (ImportantDateFormEntry) -> Unit,
) {
  Row(verticalAlignment = Alignment.Top) {
    Column(modifier = Modifier.weight(1f)) {
      OutlinedTextField(
        value = entry.label,
        onValueChange = { onUpdate(entry.copy(label = it)) },
        label = { Text("Label (e.g. Birthday)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )
      Spacer(modifier = Modifier.height(FieldSpacing))
      OutlinedTextField(
        value = entry.date,
        onValueChange = { onUpdate(entry.copy(date = it)) },
        label = { Text("Date (YYYY-MM-DD)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )
    }
    RemoveButton(onClick = onRemove)
  }
}

@Composable
private fun RemoveButton(onClick: () -> Unit) {
  IconButton(onClick = onClick) {
    Icon(
      imageVector = DayKeeperIcons.Delete,
      contentDescription = "Remove",
      tint = MaterialTheme.colorScheme.error,
    )
  }
}

@Composable
private fun FormSectionHeader(title: String, onAdd: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = FieldSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.weight(1f),
    )
    TextButton(onClick = onAdd) {
      Icon(imageVector = DayKeeperIcons.Add, contentDescription = null)
      Spacer(modifier = Modifier.width(4.dp))
      Text("Add")
    }
  }
}
