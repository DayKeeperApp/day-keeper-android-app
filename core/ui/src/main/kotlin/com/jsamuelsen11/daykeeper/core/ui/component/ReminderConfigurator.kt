package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.calendar.ReminderPreset
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderConfigurator(
  onReminderSelected: (minutesBefore: Int) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  initialMinutesBefore: Int? = null,
) {
  val sheetState = rememberModalBottomSheetState()
  var showCustomInput by remember { mutableStateOf(false) }
  var customMinutesText by remember { mutableStateOf("") }

  ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier, sheetState = sheetState) {
    Column {
      ReminderConfiguratorHeader()

      ReminderPresetList(
        initialMinutesBefore = initialMinutesBefore,
        onReminderSelected = onReminderSelected,
      )

      if (showCustomInput) {
        CustomReminderInput(
          customMinutesText = customMinutesText,
          onCustomMinutesTextChange = { customMinutesText = it },
          onReminderSelected = onReminderSelected,
        )
      } else {
        CustomReminderRow(onExpandCustomInput = { showCustomInput = true })
      }
    }
  }
}

@Composable
private fun ReminderConfiguratorHeader() {
  Row(
    modifier =
      Modifier.fillMaxWidth().padding(horizontal = ReminderConfiguratorDefaults.TITLE_PADDING),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(ReminderConfiguratorDefaults.TITLE_PADDING),
  ) {
    Icon(
      imageVector = DayKeeperIcons.Notification,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(text = ReminderConfiguratorDefaults.TITLE, style = MaterialTheme.typography.titleLarge)
  }
}

@Composable
private fun ReminderPresetList(initialMinutesBefore: Int?, onReminderSelected: (Int) -> Unit) {
  ReminderPreset.entries.forEach { preset ->
    val isSelected = initialMinutesBefore == preset.minutesBefore
    ListItem(
      headlineContent = { Text(preset.displayLabel) },
      modifier = Modifier.clickable { onReminderSelected(preset.minutesBefore) },
      trailingContent =
        if (isSelected) {
          {
            Icon(
              imageVector = DayKeeperIcons.Check,
              contentDescription = "Selected",
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        } else {
          null
        },
      colors =
        if (isSelected) {
          ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
          )
        } else {
          ListItemDefaults.colors()
        },
    )
  }
}

@Composable
private fun CustomReminderInput(
  customMinutesText: String,
  onCustomMinutesTextChange: (String) -> Unit,
  onReminderSelected: (Int) -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .padding(horizontal = ReminderConfiguratorDefaults.TITLE_PADDING)
        .padding(vertical = ReminderConfiguratorDefaults.TITLE_PADDING),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(ReminderConfiguratorDefaults.TITLE_PADDING),
  ) {
    OutlinedTextField(
      value = customMinutesText,
      onValueChange = { value -> onCustomMinutesTextChange(value.filter { it.isDigit() }) },
      modifier = Modifier.weight(1f),
      label = { Text(ReminderConfiguratorDefaults.CUSTOM_MINUTES_LABEL) },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      singleLine = true,
    )
    TextButton(
      onClick = {
        val minutes = customMinutesText.toIntOrNull()
        if (
          minutes != null &&
            minutes in
              ReminderConfiguratorDefaults.MIN_CUSTOM_MINUTES..ReminderConfiguratorDefaults
                  .MAX_CUSTOM_MINUTES
        ) {
          onReminderSelected(minutes)
        }
      }
    ) {
      Text(ReminderConfiguratorDefaults.CONFIRM_CUSTOM_LABEL)
    }
  }
}

@Composable
private fun CustomReminderRow(onExpandCustomInput: () -> Unit) {
  ListItem(
    headlineContent = { Text(ReminderConfiguratorDefaults.CUSTOM_LABEL) },
    modifier = Modifier.clickable(onClick = onExpandCustomInput),
    leadingContent = {
      Icon(
        imageVector = DayKeeperIcons.Edit,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
  )
}

object ReminderConfiguratorDefaults {
  const val TITLE = "Reminder"
  const val CUSTOM_LABEL = "Custom"
  const val CUSTOM_MINUTES_LABEL = "minutes before"
  const val CONFIRM_CUSTOM_LABEL = "Set"
  const val MIN_CUSTOM_MINUTES = 1
  const val MAX_CUSTOM_MINUTES = 40320
  val TITLE_PADDING = 16.dp
}

@Preview(showBackground = true)
@Composable
private fun ReminderConfiguratorPreview() {
  DayKeeperTheme { ReminderConfigurator(onReminderSelected = {}, onDismiss = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ReminderConfiguratorWithSelectionPreview() {
  DayKeeperTheme {
    ReminderConfigurator(onReminderSelected = {}, onDismiss = {}, initialMinutesBefore = 15)
  }
}
