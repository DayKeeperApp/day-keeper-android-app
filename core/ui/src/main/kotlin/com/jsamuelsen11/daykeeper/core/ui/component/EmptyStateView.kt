package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

private val IconSize = 72.dp
private val IconToTitleSpacing = 16.dp
private val TitleToBodySpacing = 8.dp
private val BodyToActionSpacing = 24.dp

@Composable
fun EmptyStateView(
  icon: ImageVector,
  title: String,
  modifier: Modifier = Modifier,
  body: String? = null,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      modifier = Modifier.size(IconSize),
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(IconToTitleSpacing))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
    )
    if (body != null) {
      Spacer(modifier = Modifier.height(TitleToBodySpacing))
      Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
    if (actionLabel != null && onAction != null) {
      Spacer(modifier = Modifier.height(BodyToActionSpacing))
      Button(onClick = onAction) { Text(actionLabel) }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewMinimalPreview() {
  DayKeeperTheme { EmptyStateView(icon = DayKeeperIcons.Calendar, title = "No events yet") }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewFullPreview() {
  DayKeeperTheme {
    EmptyStateView(
      icon = DayKeeperIcons.Task,
      title = "No tasks",
      body = "Create your first task to get started.",
      actionLabel = "Add Task",
      onAction = {},
    )
  }
}
