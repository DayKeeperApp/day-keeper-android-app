package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme
import com.jsamuelsen11.daykeeper.core.ui.theme.dayKeeperColors

private val DotSize = 8.dp
private val IconSize = 16.dp
private val LabelSpacing = 4.dp

@Composable
fun PriorityIndicator(
  priority: Priority,
  modifier: Modifier = Modifier,
  showLabel: Boolean = false,
) {
  val color = priorityColor(priority)
  val label = priorityLabel(priority)

  Row(
    modifier = modifier.semantics { contentDescription = "$label priority" },
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(LabelSpacing),
  ) {
    if (priority == Priority.URGENT) {
      Icon(
        imageVector = DayKeeperIcons.Flag,
        contentDescription = null,
        modifier = Modifier.size(IconSize),
        tint = color,
      )
    } else {
      Box(modifier = Modifier.size(DotSize).clip(CircleShape).background(color))
    }
    if (showLabel) {
      Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
  }
}

@Composable
private fun priorityColor(priority: Priority): Color =
  when (priority) {
    Priority.NONE -> MaterialTheme.dayKeeperColors.priority.none
    Priority.LOW -> MaterialTheme.dayKeeperColors.priority.low
    Priority.MEDIUM -> MaterialTheme.dayKeeperColors.priority.medium
    Priority.HIGH -> MaterialTheme.dayKeeperColors.priority.high
    Priority.URGENT -> MaterialTheme.dayKeeperColors.priority.urgent
  }

private fun priorityLabel(priority: Priority): String =
  when (priority) {
    Priority.NONE -> "None"
    Priority.LOW -> "Low"
    Priority.MEDIUM -> "Medium"
    Priority.HIGH -> "High"
    Priority.URGENT -> "Urgent"
  }

@Preview(showBackground = true)
@Composable
private fun PriorityIndicatorAllDotsPreview() {
  DayKeeperTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Priority.entries.forEach { priority -> PriorityIndicator(priority = priority) }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PriorityIndicatorWithLabelPreview() {
  DayKeeperTheme { PriorityIndicator(priority = Priority.HIGH, showLabel = true) }
}

@Preview(showBackground = true)
@Composable
private fun PriorityIndicatorUrgentPreview() {
  DayKeeperTheme { PriorityIndicator(priority = Priority.URGENT, showLabel = true) }
}
