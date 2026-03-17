package com.jsamuelsen11.daykeeper.feature.tasks.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.Task
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import com.jsamuelsen11.daykeeper.core.ui.component.CategoryChip
import com.jsamuelsen11.daykeeper.core.ui.component.PriorityIndicator
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme
import com.jsamuelsen11.daykeeper.feature.tasks.list.TaskListItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CardHorizontalPadding = 4.dp
private val CardContentPadding = 12.dp
private val MetaRowSpacing = 8.dp
private val PriorityCheckboxSpacing = 4.dp
private const val DUE_DATE_PREVIEW_EPOCH = 1_800_000_000_000L
private const val PREVIEW_CREATED_AT = 1_700_000_000_000L

/**
 * A compact card representing a single task in the list.
 *
 * Displays a checkbox, priority indicator, title, optional due date, and optional category chip.
 * When [item] belongs to a project, the project name is shown as secondary text.
 *
 * @param item The enriched task item to display.
 * @param onToggleComplete Called when the user taps the completion checkbox.
 * @param onClick Called when the user taps the card body.
 * @param modifier Optional modifier applied to the outer [Card].
 */
@Composable
fun TaskCard(
  item: TaskListItem,
  onToggleComplete: () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isCompleted = item.task.status == TaskStatus.DONE || item.task.status == TaskStatus.CANCELLED

  Card(modifier = modifier.fillMaxWidth().padding(horizontal = CardHorizontalPadding)) {
    Row(
      modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(CardContentPadding),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(checked = isCompleted, onCheckedChange = { onToggleComplete() })

      Spacer(modifier = Modifier.width(PriorityCheckboxSpacing))

      if (item.task.priority != Priority.NONE) {
        PriorityIndicator(priority = item.task.priority)
        Spacer(modifier = Modifier.width(MetaRowSpacing))
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.task.title,
          style = MaterialTheme.typography.bodyLarge,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
          color =
            if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
        )

        val subtitleParts = buildList {
          item.projectName?.let { add(it) }
          item.task.dueAt?.let { add(formatDueDate(it)) }
        }

        if (subtitleParts.isNotEmpty()) {
          Text(
            text = subtitleParts.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        if (item.categoryName != null) {
          val chipColor = item.categoryColor?.parseHexColor()
          CategoryChip(name = item.categoryName, color = chipColor)
        }
      }
    }
  }
}

private fun formatDueDate(epochMillis: Long): String {
  val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
  return formatter.format(Date(epochMillis))
}

private fun String.parseHexColor(): Color? =
  try {
    Color(this.toColorInt())
  } catch (_: IllegalArgumentException) {
    null
  }

@Preview(showBackground = true)
@Composable
private fun TaskCardActivePreview() {
  DayKeeperTheme {
    TaskCard(
      item =
        TaskListItem(
          task =
            Task(
              taskId = "1",
              spaceId = "s1",
              tenantId = "t1",
              title = "Design new onboarding flow",
              status = TaskStatus.IN_PROGRESS,
              priority = Priority.HIGH,
              dueAt = DUE_DATE_PREVIEW_EPOCH,
              createdAt = PREVIEW_CREATED_AT,
              updatedAt = PREVIEW_CREATED_AT,
            ),
          projectName = "Mobile App",
          categoryName = "Design",
          categoryColor = "#4CAF50",
        ),
      onToggleComplete = {},
      onClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun TaskCardCompletedPreview() {
  DayKeeperTheme {
    TaskCard(
      item =
        TaskListItem(
          task =
            Task(
              taskId = "2",
              spaceId = "s1",
              tenantId = "t1",
              title = "Write unit tests",
              status = TaskStatus.DONE,
              priority = Priority.MEDIUM,
              createdAt = PREVIEW_CREATED_AT,
              updatedAt = PREVIEW_CREATED_AT,
            ),
          projectName = null,
          categoryName = null,
          categoryColor = null,
        ),
      onToggleComplete = {},
      onClick = {},
    )
  }
}
