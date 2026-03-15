package com.jsamuelsen11.daykeeper.feature.lists.itemedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem

private val SheetPadding = 24.dp
private val SectionSpacing = 16.dp
private val ChipSpacing = 8.dp
private val ButtonSpacing = 8.dp

private val RECOMMENDED_UNITS = listOf("pcs", "kg", "g", "lb", "oz", "L", "mL")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditBottomSheet(
  item: ShoppingListItem,
  onSave: (ShoppingListItem) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var state by remember { mutableStateOf(ItemEditFormState.fromItem(item)) }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, modifier = modifier) {
    ItemEditForm(
      state = state,
      onStateChange = { state = it },
      onSave = {
        val trimmedName = state.name.trim()
        if (trimmedName.isBlank()) {
          state = state.copy(nameError = "Name cannot be empty")
          return@ItemEditForm
        }
        val parsedQty = state.quantity.toDoubleOrNull() ?: ShoppingListItem.DEFAULT_QUANTITY
        val finalUnit = state.unit.trim().ifBlank { null }
        onSave(item.copy(name = trimmedName, quantity = parsedQty, unit = finalUnit))
      },
      onCancel = onDismiss,
    )
  }
}

private data class ItemEditFormState(
  val name: String,
  val quantity: String,
  val unit: String,
  val customUnit: Boolean,
  val nameError: String?,
) {
  companion object {
    fun fromItem(item: ShoppingListItem): ItemEditFormState =
      ItemEditFormState(
        name = item.name,
        quantity = formatQuantity(item.quantity),
        unit = item.unit.orEmpty(),
        customUnit = false,
        nameError = null,
      )
  }
}

@Composable
private fun ItemEditForm(
  state: ItemEditFormState,
  onStateChange: (ItemEditFormState) -> Unit,
  onSave: () -> Unit,
  onCancel: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(SheetPadding)) {
    Text(text = "Edit Item", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(SectionSpacing))
    OutlinedTextField(
      value = state.name,
      onValueChange = { onStateChange(state.copy(name = it, nameError = null)) },
      modifier = Modifier.fillMaxWidth(),
      label = { Text("Name") },
      isError = state.nameError != null,
      supportingText = state.nameError?.let { { Text(it) } },
      singleLine = true,
    )
    Spacer(modifier = Modifier.height(SectionSpacing))
    OutlinedTextField(
      value = state.quantity,
      onValueChange = { onStateChange(state.copy(quantity = it)) },
      modifier = Modifier.fillMaxWidth(),
      label = { Text("Quantity") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
      singleLine = true,
    )
    Spacer(modifier = Modifier.height(SectionSpacing))
    UnitSelector(
      unit = state.unit,
      customUnit = state.customUnit,
      onUnitSelect = { u -> onStateChange(state.copy(unit = u, customUnit = false)) },
      onCustomUnitToggle = {
        val newUnit = if (state.unit in RECOMMENDED_UNITS) "" else state.unit
        onStateChange(state.copy(customUnit = true, unit = newUnit))
      },
      onCustomUnitChange = { onStateChange(state.copy(unit = it)) },
    )
    Spacer(modifier = Modifier.height(SectionSpacing))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      TextButton(onClick = onCancel) { Text("Cancel") }
      Spacer(modifier = Modifier.padding(start = ButtonSpacing))
      Button(onClick = onSave) { Text("Save") }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UnitSelector(
  unit: String,
  customUnit: Boolean,
  onUnitSelect: (String) -> Unit,
  onCustomUnitToggle: () -> Unit,
  onCustomUnitChange: (String) -> Unit,
) {
  Text(text = "Unit", style = MaterialTheme.typography.labelMedium)
  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(ChipSpacing),
    verticalArrangement = Arrangement.spacedBy(ChipSpacing),
  ) {
    RECOMMENDED_UNITS.forEach { u ->
      FilterChip(
        selected = unit == u && !customUnit,
        onClick = { onUnitSelect(u) },
        label = { Text(u) },
      )
    }
    FilterChip(selected = customUnit, onClick = onCustomUnitToggle, label = { Text("Custom") })
  }
  if (customUnit) {
    Spacer(modifier = Modifier.height(ButtonSpacing))
    OutlinedTextField(
      value = unit,
      onValueChange = onCustomUnitChange,
      modifier = Modifier.fillMaxWidth(),
      label = { Text("Custom unit") },
      singleLine = true,
    )
  }
}

private fun formatQuantity(quantity: Double): String {
  return if (quantity == quantity.toLong().toDouble()) {
    quantity.toLong().toString()
  } else {
    quantity.toString()
  }
}
