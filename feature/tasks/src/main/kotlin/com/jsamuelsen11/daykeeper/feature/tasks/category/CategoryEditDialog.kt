package com.jsamuelsen11.daykeeper.feature.tasks.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.component.ColorPickerDialog

private val ColorSwatchSize = 24.dp
private val FieldSpacing = 12.dp

@Composable
fun CategoryEditDialog(
  onSave: (name: String, color: String?) -> Unit,
  onDismiss: () -> Unit,
  initialName: String = "",
  initialColor: String? = null,
) {
  var name by remember { mutableStateOf(initialName) }
  var colorHex by remember { mutableStateOf(initialColor) }
  var nameError by remember { mutableStateOf<String?>(null) }
  var showColorPicker by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initialName.isBlank()) "New Category" else "Edit Category") },
    text = {
      CategoryEditDialogContent(
        name = name,
        nameError = nameError,
        colorHex = colorHex,
        onNameChange = {
          name = it
          nameError = null
        },
        onPickColor = { showColorPicker = true },
      )
    },
    confirmButton = {
      TextButton(
        onClick = {
          val trimmed = name.trim()
          if (trimmed.isBlank()) nameError = "Name is required" else onSave(trimmed, colorHex)
        }
      ) {
        Text("Save")
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )

  if (showColorPicker) {
    ColorPickerDialog(
      onColorSelected = { color ->
        colorHex = colorToHex(color)
        showColorPicker = false
      },
      onDismiss = { showColorPicker = false },
      selectedColor = colorHex?.let { parseHexColor(it) },
    )
  }
}

@Composable
private fun CategoryEditDialogContent(
  name: String,
  nameError: String?,
  colorHex: String?,
  onNameChange: (String) -> Unit,
  onPickColor: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(FieldSpacing)) {
    OutlinedTextField(
      value = name,
      onValueChange = onNameChange,
      label = { Text("Name") },
      isError = nameError != null,
      supportingText = nameError?.let { { Text(it) } },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(FieldSpacing),
    ) {
      if (colorHex != null) {
        val color = parseHexColor(colorHex)
        if (color != null) {
          Box(modifier = Modifier.size(ColorSwatchSize).clip(CircleShape).background(color))
        }
      }
      OutlinedButton(onClick = onPickColor) { Text("Choose Color") }
    }
  }
}

private fun parseHexColor(hex: String): Color? =
  runCatching {
      val cleaned = hex.trimStart('#')
      val argb =
        when (cleaned.length) {
          HEX_RGB_LENGTH -> "FF$cleaned".toLong(radix = HEX_RADIX)
          HEX_ARGB_LENGTH -> cleaned.toLong(radix = HEX_RADIX)
          else -> return@runCatching null
        }
      Color(argb.toInt())
    }
    .getOrNull()

private fun colorToHex(color: Color): String {
  val r = (color.red * MAX_COLOR_VALUE).toInt()
  val g = (color.green * MAX_COLOR_VALUE).toInt()
  val b = (color.blue * MAX_COLOR_VALUE).toInt()
  return "#%02X%02X%02X".format(r, g, b)
}

private const val HEX_RGB_LENGTH = 6
private const val HEX_ARGB_LENGTH = 8
private const val HEX_RADIX = 16
private const val MAX_COLOR_VALUE = 255f
