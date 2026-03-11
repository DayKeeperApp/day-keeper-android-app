package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.CalendarBlue
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme
import com.jsamuelsen11.daykeeper.core.ui.theme.dayKeeperColors

@Composable
fun ColorPickerDialog(
  onColorSelected: (Color) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  selectedColor: Color? = null,
  colors: List<Color>? = null,
  title: String = ColorPickerDialogDefaults.TITLE,
) {
  val palette = colors ?: MaterialTheme.dayKeeperColors.calendar.palette

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {},
    modifier = modifier,
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(ColorPickerDialogDefaults.DISMISS_LABEL) }
    },
    title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
    text = {
      LazyVerticalGrid(
        columns = GridCells.Fixed(ColorPickerDialogDefaults.COLUMNS),
        horizontalArrangement = Arrangement.spacedBy(ColorPickerDialogDefaults.SWATCH_SPACING),
        verticalArrangement = Arrangement.spacedBy(ColorPickerDialogDefaults.SWATCH_SPACING),
        modifier = Modifier.padding(top = ColorPickerDialogDefaults.SWATCH_SPACING),
      ) {
        items(palette) { color ->
          ColorSwatch(
            color = color,
            isSelected = color == selectedColor,
            onClick = { onColorSelected(color) },
          )
        }
      }
    },
  )
}

@Composable
private fun ColorSwatch(color: Color, isSelected: Boolean, onClick: () -> Unit) {
  Box(
    modifier =
      Modifier.size(ColorPickerDialogDefaults.COLOR_SWATCH_SIZE)
        .clip(CircleShape)
        .background(color)
        .clickable(onClick = onClick)
        .semantics { contentDescription = "Color swatch" },
    contentAlignment = Alignment.Center,
  ) {
    if (isSelected) {
      Icon(
        imageVector = DayKeeperIcons.Check,
        contentDescription = "Selected",
        tint = ColorPickerDialogDefaults.checkColor(color),
      )
    }
  }
}

object ColorPickerDialogDefaults {
  const val TITLE = "Choose color"
  const val DISMISS_LABEL = "Cancel"
  const val COLUMNS = 4
  val COLOR_SWATCH_SIZE = 48.dp
  val SWATCH_SPACING = 8.dp
  val GRID_PADDING = 16.dp
  private const val LUMINANCE_THRESHOLD = 0.5f

  fun checkColor(swatchColor: Color): Color =
    if (swatchColor.luminance() > LUMINANCE_THRESHOLD) Color.Black else Color.White
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerDialogPreview() {
  DayKeeperTheme { ColorPickerDialog(onColorSelected = {}, onDismiss = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerDialogWithSelectionPreview() {
  DayKeeperTheme {
    ColorPickerDialog(onColorSelected = {}, onDismiss = {}, selectedColor = CalendarBlue)
  }
}
