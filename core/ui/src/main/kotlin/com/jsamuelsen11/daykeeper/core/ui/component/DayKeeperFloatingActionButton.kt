package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

@Composable
fun DayKeeperFloatingActionButton(
  onClick: () -> Unit,
  icon: ImageVector,
  contentDescription: String,
  modifier: Modifier = Modifier,
  text: String? = null,
  containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
  contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
  if (text != null) {
    ExtendedFloatingActionButton(
      onClick = onClick,
      modifier = modifier,
      containerColor = containerColor,
      contentColor = contentColor,
      icon = { Icon(imageVector = icon, contentDescription = contentDescription) },
      text = { Text(text) },
    )
  } else {
    FloatingActionButton(
      onClick = onClick,
      modifier = modifier,
      containerColor = containerColor,
      contentColor = contentColor,
    ) {
      Icon(imageVector = icon, contentDescription = contentDescription)
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DayKeeperFabIconOnlyPreview() {
  DayKeeperTheme {
    DayKeeperFloatingActionButton(
      onClick = {},
      icon = DayKeeperIcons.Add,
      contentDescription = "Add",
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun DayKeeperFabExtendedPreview() {
  DayKeeperTheme {
    DayKeeperFloatingActionButton(
      onClick = {},
      icon = DayKeeperIcons.Add,
      contentDescription = "New Task",
      text = "New Task",
    )
  }
}
