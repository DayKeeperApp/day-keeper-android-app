package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

private val ColorDotSize = 8.dp
private val DisplayChipHorizontalPadding = 12.dp
private val DisplayChipVerticalPadding = 6.dp
private val DisplayChipSpacing = 6.dp
private val DismissIconSize = 16.dp
private val PreviewGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
  name: String,
  modifier: Modifier = Modifier,
  color: Color? = null,
  selected: Boolean = false,
  onClick: (() -> Unit)? = null,
  onDismiss: (() -> Unit)? = null,
) {
  if (onClick != null) {
    FilterChip(
      selected = selected,
      onClick = onClick,
      label = { Text(text = name) },
      modifier = modifier,
      leadingIcon = { ChipLeadingContent(color) },
      trailingIcon =
        if (onDismiss != null) {
          {
            IconButton(onClick = onDismiss, modifier = Modifier.size(DismissIconSize)) {
              Icon(
                imageVector = DayKeeperIcons.Close,
                contentDescription = "Remove $name",
                modifier = Modifier.size(DismissIconSize),
              )
            }
          }
        } else {
          null
        },
    )
  } else {
    DisplayOnlyChip(name = name, modifier = modifier, color = color)
  }
}

@Composable
private fun ChipLeadingContent(color: Color?) {
  if (color != null) {
    Box(modifier = Modifier.size(ColorDotSize).clip(CircleShape).background(color))
  } else {
    Icon(
      imageVector = DayKeeperIcons.Label,
      contentDescription = null,
      modifier = Modifier.size(FilterChipDefaults.IconSize),
    )
  }
}

@Composable
private fun DisplayOnlyChip(name: String, modifier: Modifier = Modifier, color: Color? = null) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.small,
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
  ) {
    Row(
      modifier =
        Modifier.padding(
          horizontal = DisplayChipHorizontalPadding,
          vertical = DisplayChipVerticalPadding,
        ),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(DisplayChipSpacing),
    ) {
      ChipLeadingContent(color)
      Text(text = name, style = MaterialTheme.typography.labelMedium)
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun CategoryChipDefaultPreview() {
  DayKeeperTheme { CategoryChip(name = "Work") }
}

@Preview(showBackground = true)
@Composable
private fun CategoryChipWithColorPreview() {
  DayKeeperTheme { CategoryChip(name = "Health", color = PreviewGreen) }
}

@Preview(showBackground = true)
@Composable
private fun CategoryChipSelectedPreview() {
  DayKeeperTheme { CategoryChip(name = "Errands", selected = true, onClick = {}) }
}

@Preview(showBackground = true)
@Composable
private fun CategoryChipDismissablePreview() {
  DayKeeperTheme { CategoryChip(name = "Shopping", onClick = {}, onDismiss = {}) }
}
