package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

val DragHandleSize = 24.dp
val DragHandleStartPadding = 4.dp
val ContentStartPadding = 8.dp
val ItemMinHeight = 48.dp

@Composable
fun DraggableListItem(
  modifier: Modifier = Modifier,
  dragModifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) {
  Row(
    modifier = modifier.heightIn(min = ItemMinHeight),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = DayKeeperIcons.DragHandle,
      contentDescription = "Drag to reorder",
      modifier = dragModifier.padding(start = DragHandleStartPadding).size(DragHandleSize),
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
      modifier = Modifier.weight(1f).padding(start = ContentStartPadding),
      verticalAlignment = Alignment.CenterVertically,
      content = content,
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun DraggableListItemPreview() {
  DayKeeperTheme {
    DraggableListItem {
      Text(text = "Reorderable item", style = MaterialTheme.typography.bodyLarge)
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DraggableListItemMultilinePreview() {
  DayKeeperTheme {
    DraggableListItem {
      androidx.compose.foundation.layout.Column {
        Text(text = "Primary text", style = MaterialTheme.typography.bodyLarge)
        Text(
          text = "Secondary text",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
