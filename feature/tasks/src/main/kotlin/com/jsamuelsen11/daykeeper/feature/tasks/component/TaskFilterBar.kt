package com.jsamuelsen11.daykeeper.feature.tasks.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme
import com.jsamuelsen11.daykeeper.feature.tasks.list.CategoryOption
import com.jsamuelsen11.daykeeper.feature.tasks.list.SortOrder
import com.jsamuelsen11.daykeeper.feature.tasks.list.TaskFilters

private val FilterBarHorizontalPadding = 16.dp
private val FilterBarVerticalPadding = 4.dp
private val FilterChipSpacing = 8.dp

/**
 * A horizontally scrollable row of [FilterChip]s for status, priority, category, and sort order.
 *
 * Each chip expands into a [DropdownMenu] when tapped, letting the user pick a single value. Active
 * filters are reflected through the chip's `selected` state.
 *
 * @param filters The currently active filter state.
 * @param categories Available category options to display in the category dropdown.
 * @param onStatusFilterChanged Called when the user selects or clears a status filter.
 * @param onPriorityFilterChanged Called when the user selects or clears a priority filter.
 * @param onCategoryFilterChanged Called when the user selects or clears a category filter.
 * @param onSortOrderChanged Called when the user changes the sort order.
 * @param modifier Optional modifier applied to the outer [Row].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFilterBar(
  filters: TaskFilters,
  categories: List<CategoryOption>,
  onStatusFilterChanged: (Set<TaskStatus>) -> Unit,
  onPriorityFilterChanged: (Set<Priority>) -> Unit,
  onCategoryFilterChanged: (String?) -> Unit,
  onSortOrderChanged: (SortOrder) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = FilterBarHorizontalPadding, vertical = FilterBarVerticalPadding),
    horizontalArrangement = Arrangement.spacedBy(FilterChipSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    StatusFilterChip(activeStatuses = filters.statuses, onChanged = onStatusFilterChanged)

    PriorityFilterChip(activePriorities = filters.priorities, onChanged = onPriorityFilterChanged)

    if (categories.isNotEmpty()) {
      CategoryFilterChip(
        activeCategoryId = filters.categoryId,
        categories = categories,
        onChanged = onCategoryFilterChanged,
      )
    }

    SortOrderChip(current = filters.sortOrder, onChanged = onSortOrderChanged)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterChip(
  activeStatuses: Set<TaskStatus>,
  onChanged: (Set<TaskStatus>) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  val label =
    when {
      activeStatuses.size == 1 -> activeStatuses.first().displayName()
      activeStatuses.size > 1 -> "Status (${activeStatuses.size})"
      else -> "Status"
    }

  FilterChip(
    selected = activeStatuses.isNotEmpty(),
    onClick = { expanded = true },
    label = { Text(label) },
    modifier = modifier,
    trailingIcon = {
      Icon(
        imageVector = if (expanded) DayKeeperIcons.ExpandLess else DayKeeperIcons.ExpandMore,
        contentDescription = null,
      )
    },
  )

  DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
    DropdownMenuItem(
      text = { Text("All statuses") },
      onClick = {
        onChanged(emptySet())
        expanded = false
      },
    )
    TaskStatus.entries.forEach { status ->
      DropdownMenuItem(
        text = { Text(status.displayName()) },
        onClick = {
          onChanged(setOf(status))
          expanded = false
        },
        trailingIcon = {
          if (status in activeStatuses) {
            Icon(imageVector = DayKeeperIcons.Check, contentDescription = null)
          }
        },
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityFilterChip(
  activePriorities: Set<Priority>,
  onChanged: (Set<Priority>) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  val label =
    when {
      activePriorities.size == 1 -> activePriorities.first().displayName()
      activePriorities.size > 1 -> "Priority (${activePriorities.size})"
      else -> "Priority"
    }

  FilterChip(
    selected = activePriorities.isNotEmpty(),
    onClick = { expanded = true },
    label = { Text(label) },
    modifier = modifier,
    trailingIcon = {
      Icon(
        imageVector = if (expanded) DayKeeperIcons.ExpandLess else DayKeeperIcons.ExpandMore,
        contentDescription = null,
      )
    },
  )

  DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
    DropdownMenuItem(
      text = { Text("All priorities") },
      onClick = {
        onChanged(emptySet())
        expanded = false
      },
    )
    Priority.entries.forEach { priority ->
      DropdownMenuItem(
        text = { Text(priority.displayName()) },
        onClick = {
          onChanged(setOf(priority))
          expanded = false
        },
        trailingIcon = {
          if (priority in activePriorities) {
            Icon(imageVector = DayKeeperIcons.Check, contentDescription = null)
          }
        },
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterChip(
  activeCategoryId: String?,
  categories: List<CategoryOption>,
  onChanged: (String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  val activeName = categories.firstOrNull { it.categoryId == activeCategoryId }?.name
  val label = activeName ?: "Category"

  FilterChip(
    selected = activeCategoryId != null,
    onClick = { expanded = true },
    label = { Text(label) },
    modifier = modifier,
    leadingIcon = { Icon(imageVector = DayKeeperIcons.Label, contentDescription = null) },
    trailingIcon = {
      Icon(
        imageVector = if (expanded) DayKeeperIcons.ExpandLess else DayKeeperIcons.ExpandMore,
        contentDescription = null,
      )
    },
  )

  DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
    DropdownMenuItem(
      text = { Text("All categories") },
      onClick = {
        onChanged(null)
        expanded = false
      },
    )
    categories.forEach { option ->
      DropdownMenuItem(
        text = { Text(option.name) },
        onClick = {
          onChanged(option.categoryId)
          expanded = false
        },
        trailingIcon = {
          if (option.categoryId == activeCategoryId) {
            Icon(imageVector = DayKeeperIcons.Check, contentDescription = null)
          }
        },
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortOrderChip(
  current: SortOrder,
  onChanged: (SortOrder) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  val label = current.displayName()

  FilterChip(
    selected = current != SortOrder.DUE_DATE,
    onClick = { expanded = true },
    label = { Text(label) },
    modifier = modifier,
    leadingIcon = { Icon(imageVector = DayKeeperIcons.Sort, contentDescription = null) },
    trailingIcon = {
      Icon(
        imageVector = if (expanded) DayKeeperIcons.ExpandLess else DayKeeperIcons.ExpandMore,
        contentDescription = null,
      )
    },
  )

  DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
    SortOrder.entries.forEach { order ->
      DropdownMenuItem(
        text = { Text(order.displayName()) },
        onClick = {
          onChanged(order)
          expanded = false
        },
        trailingIcon = {
          if (order == current) {
            Icon(imageVector = DayKeeperIcons.Check, contentDescription = null)
          }
        },
      )
    }
  }
}

private fun TaskStatus.displayName(): String =
  when (this) {
    TaskStatus.TODO -> "To Do"
    TaskStatus.IN_PROGRESS -> "In Progress"
    TaskStatus.DONE -> "Done"
    TaskStatus.CANCELLED -> "Cancelled"
  }

private fun Priority.displayName(): String =
  when (this) {
    Priority.NONE -> "None"
    Priority.LOW -> "Low"
    Priority.MEDIUM -> "Medium"
    Priority.HIGH -> "High"
    Priority.URGENT -> "Urgent"
  }

private fun SortOrder.displayName(): String =
  when (this) {
    SortOrder.DUE_DATE -> "Due Date"
    SortOrder.PRIORITY -> "Priority"
    SortOrder.RECENTLY_ADDED -> "Recently Added"
  }

@Preview(showBackground = true)
@Composable
private fun TaskFilterBarDefaultPreview() {
  DayKeeperTheme {
    TaskFilterBar(
      filters = TaskFilters(),
      categories =
        listOf(
          CategoryOption("c1", "Work", "#2196F3"),
          CategoryOption("c2", "Personal", "#4CAF50"),
        ),
      onStatusFilterChanged = {},
      onPriorityFilterChanged = {},
      onCategoryFilterChanged = {},
      onSortOrderChanged = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun TaskFilterBarActiveFiltersPreview() {
  DayKeeperTheme {
    TaskFilterBar(
      filters =
        TaskFilters(
          statuses = setOf(TaskStatus.IN_PROGRESS),
          priorities = setOf(Priority.HIGH),
          categoryId = "c1",
          sortOrder = SortOrder.PRIORITY,
        ),
      categories = listOf(CategoryOption("c1", "Work", "#2196F3")),
      onStatusFilterChanged = {},
      onPriorityFilterChanged = {},
      onCategoryFilterChanged = {},
      onSortOrderChanged = {},
    )
  }
}
