package com.jsamuelsen11.daykeeper.core.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.Priority
import com.jsamuelsen11.daykeeper.core.model.SpaceRole
import com.jsamuelsen11.daykeeper.core.model.SpaceType
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

private val SectionSpacing = 24.dp
private val SectionHeaderBottomPadding = 12.dp
private val ItemSpacing = 16.dp
private val ContentPadding = 16.dp
private val PlaceholderVerticalPadding = 20.dp
private val PlaceholderIconSpacing = 8.dp
private val ConstrainedHeight = 200.dp
private val LoadingIndicatorHeight = 100.dp
private val ChipRowSpacing = 8.dp
private val BadgeRowSpacing = 8.dp
private val PreviewBlue = Color(0xFF2196F3)
private val PreviewGreen = Color(0xFF4CAF50)

/**
 * Scrollable showcase of all Day Keeper design system components. Open this file in Android Studio
 * to see all components rendered together in the preview pane.
 */
@Composable
fun DayKeeperComponentCatalog(modifier: Modifier = Modifier) {
  Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(ContentPadding)) {
    NavigationSection()
    ButtonsSection()
    BadgesChipsIndicatorsSection()
    ListItemsSection()
    FeedbackSection()
    DialogsPickersSection()
  }
}

// region Navigation & App Chrome

@Composable
private fun NavigationSection() {
  CatalogSectionHeader(title = "Navigation & App Chrome")
  DayKeeperTopAppBar(title = "Day Keeper")
  Spacer(modifier = Modifier.height(ItemSpacing))
  DayKeeperTopAppBar(
    title = "Event Details",
    onNavigationClick = {},
    actions = {
      IconButton(onClick = {}) {
        Icon(imageVector = DayKeeperIcons.Edit, contentDescription = "Edit")
      }
      IconButton(onClick = {}) {
        Icon(imageVector = DayKeeperIcons.Delete, contentDescription = "Delete")
      }
    },
  )
  Spacer(modifier = Modifier.height(ItemSpacing))
  DayKeeperSearchBar(query = "", onQueryChange = {})
  Spacer(modifier = Modifier.height(ItemSpacing))
  DayKeeperSearchBar(query = "tasks", onQueryChange = {}) {
    CategoryChip(name = "Work", color = PreviewBlue, selected = true, onClick = {})
    CategoryChip(name = "Personal", onClick = {})
    CategoryChip(name = "Health", color = PreviewGreen, onClick = {})
  }
  Spacer(modifier = Modifier.height(SectionSpacing))
}

// endregion

// region Buttons & Actions

@Composable
private fun ButtonsSection() {
  CatalogSectionHeader(title = "Buttons & Actions")
  Row(horizontalArrangement = Arrangement.spacedBy(ItemSpacing)) {
    DayKeeperFloatingActionButton(
      onClick = {},
      icon = DayKeeperIcons.Add,
      contentDescription = "Add",
    )
    DayKeeperFloatingActionButton(
      onClick = {},
      icon = DayKeeperIcons.Add,
      contentDescription = "New Task",
      text = "New Task",
    )
  }
  Spacer(modifier = Modifier.height(SectionSpacing))
}

// endregion

// region Badges, Chips & Indicators

@Composable
private fun BadgesChipsIndicatorsSection() {
  CatalogSectionHeader(title = "Badges, Chips & Indicators")

  Text(text = "Space Badges", style = MaterialTheme.typography.labelLarge)
  Spacer(modifier = Modifier.height(ItemSpacing))
  Row(horizontalArrangement = Arrangement.spacedBy(BadgeRowSpacing)) {
    SpaceBadge(spaceName = "Personal", spaceType = SpaceType.PERSONAL)
    SpaceBadge(spaceName = "Family", spaceType = SpaceType.SHARED, role = SpaceRole.EDITOR)
    SpaceBadge(spaceName = "Holidays", spaceType = SpaceType.SYSTEM)
  }

  Spacer(modifier = Modifier.height(ItemSpacing))
  Text(text = "Priority Indicators", style = MaterialTheme.typography.labelLarge)
  Spacer(modifier = Modifier.height(ItemSpacing))
  Row(horizontalArrangement = Arrangement.spacedBy(ChipRowSpacing)) {
    Priority.entries.forEach { priority -> PriorityIndicator(priority = priority) }
  }
  Spacer(modifier = Modifier.height(ItemSpacing))
  PriorityIndicator(priority = Priority.URGENT, showLabel = true)

  Spacer(modifier = Modifier.height(ItemSpacing))
  Text(text = "Category Chips", style = MaterialTheme.typography.labelLarge)
  Spacer(modifier = Modifier.height(ItemSpacing))
  Row(horizontalArrangement = Arrangement.spacedBy(ChipRowSpacing)) {
    CategoryChip(name = "Work")
    CategoryChip(name = "Health", color = PreviewGreen)
    CategoryChip(name = "Errands", selected = true, onClick = {})
    CategoryChip(name = "Shopping", onClick = {}, onDismiss = {})
  }
  Spacer(modifier = Modifier.height(SectionSpacing))
}

// endregion

// region List Items

@Composable
private fun ListItemsSection() {
  CatalogSectionHeader(title = "List Items")

  Text(text = "Swipeable List Item", style = MaterialTheme.typography.labelLarge)
  Spacer(modifier = Modifier.height(ItemSpacing))
  SwipeableListItem(onComplete = {}, onDelete = {}) {
    Text(
      text = "Buy groceries",
      modifier = Modifier.padding(horizontal = SwipeBackgroundPadding, vertical = SwipeIconSize),
      style = MaterialTheme.typography.bodyLarge,
    )
  }

  Spacer(modifier = Modifier.height(ItemSpacing))
  Text(text = "Draggable List Item", style = MaterialTheme.typography.labelLarge)
  Spacer(modifier = Modifier.height(ItemSpacing))
  DraggableListItem { Text(text = "Reorderable item", style = MaterialTheme.typography.bodyLarge) }
  Spacer(modifier = Modifier.height(ItemSpacing))
  DraggableListItem {
    Column {
      Text(text = "Primary text", style = MaterialTheme.typography.bodyLarge)
      Text(
        text = "Secondary text",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
  Spacer(modifier = Modifier.height(SectionSpacing))
}

// endregion

// region Feedback & Empty States

@Composable
private fun FeedbackSection() {
  CatalogSectionHeader(title = "Feedback & Empty States")

  Text(text = "Loading Indicator", style = MaterialTheme.typography.labelLarge)
  Spacer(modifier = Modifier.height(ItemSpacing))
  Box(modifier = Modifier.fillMaxWidth().height(LoadingIndicatorHeight)) { LoadingIndicator() }

  Spacer(modifier = Modifier.height(ItemSpacing))
  Text(text = "Empty State View", style = MaterialTheme.typography.labelLarge)
  Spacer(modifier = Modifier.height(ItemSpacing))
  Box(modifier = Modifier.fillMaxWidth().height(ConstrainedHeight)) {
    EmptyStateView(
      icon = DayKeeperIcons.Task,
      title = "No tasks",
      body = "Create your first task to get started.",
      actionLabel = "Add Task",
      onAction = {},
    )
  }
  Spacer(modifier = Modifier.height(SectionSpacing))
}

// endregion

// region Dialogs & Pickers

@Composable
private fun DialogsPickersSection() {
  CatalogSectionHeader(title = "Dialogs & Pickers")
  CatalogPlaceholder(
    componentName = "ConfirmationDialog",
    note = "AlertDialog — see ConfirmationDialog previews",
  )
  Spacer(modifier = Modifier.height(ItemSpacing))
  CatalogPlaceholder(
    componentName = "ColorPickerDialog",
    note = "AlertDialog with color grid — see ColorPickerDialog previews",
  )
  Spacer(modifier = Modifier.height(ItemSpacing))
  CatalogPlaceholder(
    componentName = "DayKeeperDatePicker / TimePicker",
    note = "DatePickerDialog / AlertDialog — see DayKeeperDateTimePicker previews",
  )
  Spacer(modifier = Modifier.height(ItemSpacing))
  CatalogPlaceholder(
    componentName = "RecurrencePicker",
    note = "ModalBottomSheet — see RecurrencePicker previews",
  )
  Spacer(modifier = Modifier.height(ItemSpacing))
  CatalogPlaceholder(
    componentName = "ReminderConfigurator",
    note = "ModalBottomSheet — see ReminderConfigurator previews",
  )
}

// endregion

// region Helpers

@Composable
private fun CatalogSectionHeader(title: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    HorizontalDivider()
    Spacer(modifier = Modifier.height(SectionHeaderBottomPadding))
    Text(text = title, style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(SectionHeaderBottomPadding))
  }
}

@Composable
private fun CatalogPlaceholder(componentName: String, note: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
  ) {
    Row(
      modifier = Modifier.padding(ContentPadding),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(PlaceholderIconSpacing),
    ) {
      Icon(
        imageVector = DayKeeperIcons.Info,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Column(modifier = Modifier.padding(vertical = PlaceholderVerticalPadding)) {
        Text(text = componentName, style = MaterialTheme.typography.titleSmall)
        Text(
          text = note,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

// endregion

// region Previews

@Preview(name = "Component Catalog", showBackground = true, showSystemUi = true)
@Preview(
  name = "Component Catalog — Dark",
  showBackground = true,
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ComponentCatalogPreview() {
  DayKeeperTheme { DayKeeperComponentCatalog() }
}

// endregion
