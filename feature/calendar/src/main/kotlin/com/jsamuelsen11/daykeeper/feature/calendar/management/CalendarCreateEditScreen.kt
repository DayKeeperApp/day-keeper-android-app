package com.jsamuelsen11.daykeeper.feature.calendar.management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.ColorPickerDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.feature.calendar.component.parseHexColor
import org.koin.compose.viewmodel.koinViewModel

private val ContentPadding = 16.dp
private val SectionSpacing = 20.dp
private val ColorSwatchSize = 40.dp
private val ColorRowSpacing = 12.dp
private const val LABEL_NEW_CALENDAR = "New Calendar"
private const val LABEL_EDIT_CALENDAR = "Edit Calendar"
private const val LABEL_NAME = "Name"
private const val LABEL_COLOR = "Color"
private const val LABEL_CHANGE_COLOR = "Change color"
private const val LABEL_DEFAULT_CALENDAR = "Default calendar"
private const val LABEL_SAVE = "Save"
private const val COLOR_CHANNEL_MAX = 255

@Composable
fun CalendarCreateEditScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CalendarCreateEditViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
      when (event) {
        CalendarCreateEditEvent.Saved -> onNavigateBack()
      }
    }
  }

  val title =
    when (val state = uiState) {
      is CalendarCreateEditUiState.Ready ->
        if (state.isEditing) LABEL_EDIT_CALENDAR else LABEL_NEW_CALENDAR
      else -> LABEL_NEW_CALENDAR
    }

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = title, onNavigationClick = onNavigateBack) },
  ) { innerPadding ->
    when (val state = uiState) {
      is CalendarCreateEditUiState.Loading ->
        LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
      is CalendarCreateEditUiState.Ready ->
        CalendarFormContent(
          state = state,
          onNameChanged = viewModel::onNameChanged,
          onColorSelected = viewModel::onColorSelected,
          onDefaultToggled = viewModel::onDefaultToggled,
          onSave = viewModel::onSave,
          modifier = Modifier.padding(innerPadding),
        )
    }
  }
}

@Composable
private fun CalendarFormContent(
  state: CalendarCreateEditUiState.Ready,
  onNameChanged: (String) -> Unit,
  onColorSelected: (String) -> Unit,
  onDefaultToggled: (Boolean) -> Unit,
  onSave: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showColorPicker by remember { mutableStateOf(false) }

  if (showColorPicker) {
    ColorPickerDialog(
      selectedColor = parseHexColor(state.color),
      onColorSelected = { color ->
        onColorSelected(colorToHex(color))
        showColorPicker = false
      },
      onDismiss = { showColorPicker = false },
    )
  }

  Column(
    modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(ContentPadding),
    verticalArrangement = Arrangement.spacedBy(SectionSpacing),
  ) {
    OutlinedTextField(
      value = state.name,
      onValueChange = onNameChanged,
      label = { Text(LABEL_NAME) },
      isError = state.nameError != null,
      supportingText = state.nameError?.let { error -> { Text(error) } },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    ColorPickerRow(color = state.color, onShowPicker = { showColorPicker = true })
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = LABEL_DEFAULT_CALENDAR,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.weight(1f),
      )
      Switch(checked = state.isDefault, onCheckedChange = onDefaultToggled)
    }
    Spacer(modifier = Modifier.height(SectionSpacing))
    Button(onClick = onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
      Text(LABEL_SAVE)
    }
  }
}

@Composable
private fun ColorPickerRow(color: String, onShowPicker: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onShowPicker),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(ColorRowSpacing),
  ) {
    val parsedColor = remember(color) { parseHexColor(color) }
    Box(
      modifier =
        Modifier.size(ColorSwatchSize)
          .clip(CircleShape)
          .background(parsedColor ?: MaterialTheme.colorScheme.primary)
    )
    Column {
      Text(text = LABEL_COLOR, style = MaterialTheme.typography.bodyLarge)
      Text(
        text = LABEL_CHANGE_COLOR,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

private fun colorToHex(color: Color): String {
  val red = (color.red * COLOR_CHANNEL_MAX).toInt()
  val green = (color.green * COLOR_CHANNEL_MAX).toInt()
  val blue = (color.blue * COLOR_CHANNEL_MAX).toInt()
  return "#%02X%02X%02X".format(red, green, blue)
}
