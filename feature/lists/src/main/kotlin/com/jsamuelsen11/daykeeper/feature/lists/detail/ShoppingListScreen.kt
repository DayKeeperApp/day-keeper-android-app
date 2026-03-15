package com.jsamuelsen11.daykeeper.feature.lists.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.model.list.ShoppingListItem
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.component.SwipeableListItem
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.lists.itemedit.ItemEditBottomSheet
import org.koin.compose.viewmodel.koinViewModel

private val QuickAddPadding = 16.dp
private val ItemRowHorizontalPadding = 16.dp
private val ItemRowVerticalPadding = 4.dp
private val CheckedHeaderPadding = 16.dp
private val ClearButtonPadding = 8.dp

@Composable
fun ShoppingListScreen(
  onNavigateBack: () -> Unit,
  onEditList: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ShoppingListViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier,
    topBar = {
      val title =
        when (val state = uiState) {
          is ShoppingListUiState.Success -> state.list.name
          else -> "List"
        }
      DayKeeperTopAppBar(
        title = title,
        onNavigationClick = onNavigateBack,
        actions = {
          IconButton(onClick = onEditList) {
            Icon(imageVector = DayKeeperIcons.Edit, contentDescription = "Edit list")
          }
        },
      )
    },
  ) { innerPadding ->
    when (val state = uiState) {
      is ShoppingListUiState.Loading ->
        LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
      is ShoppingListUiState.Error ->
        EmptyStateView(
          icon = DayKeeperIcons.Lists,
          title = "Error",
          body = state.message,
          modifier = Modifier.padding(innerPadding),
        )
      is ShoppingListUiState.Success -> {
        ShoppingListContent(
          state = state,
          onAddItem = viewModel::addItem,
          onToggleItem = viewModel::toggleItem,
          onDeleteItem = viewModel::deleteItem,
          onEditItem = viewModel::editItem,
          onUpdateItem = viewModel::updateItem,
          onToggleCheckedExpanded = viewModel::toggleCheckedExpanded,
          onClearChecked = viewModel::clearChecked,
          modifier = Modifier.padding(innerPadding),
        )
      }
    }
  }
}

@Composable
private fun ShoppingListContent(
  state: ShoppingListUiState.Success,
  onAddItem: (String) -> Unit,
  onToggleItem: (String, Boolean) -> Unit,
  onDeleteItem: (String) -> Unit,
  onEditItem: (ShoppingListItem?) -> Unit,
  onUpdateItem: (ShoppingListItem) -> Unit,
  onToggleCheckedExpanded: () -> Unit,
  onClearChecked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize()) {
    QuickAddField(onAdd = onAddItem)
    HorizontalDivider()
    if (state.uncheckedItems.isEmpty() && state.checkedItems.isEmpty()) {
      EmptyStateView(
        icon = DayKeeperIcons.Lists,
        title = "No items yet",
        body = "Type above to add items.",
      )
    } else {
      LazyColumn(modifier = Modifier.weight(1f)) {
        items(items = state.uncheckedItems, key = { it.itemId }) { item ->
          SwipeableListItem(onDelete = { onDeleteItem(item.itemId) }) {
            ShoppingListItemRow(
              item = item,
              onToggle = { onToggleItem(item.itemId, true) },
              onClick = { onEditItem(item) },
            )
          }
        }
        if (state.checkedItems.isNotEmpty()) {
          item(key = "checked_header") {
            CheckedSectionHeader(
              count = state.checkedItems.size,
              expanded = state.checkedExpanded,
              onToggle = onToggleCheckedExpanded,
              onClear = onClearChecked,
            )
          }
          if (state.checkedExpanded) {
            items(items = state.checkedItems, key = { it.itemId }) { item ->
              SwipeableListItem(onDelete = { onDeleteItem(item.itemId) }) {
                ShoppingListItemRow(
                  item = item,
                  onToggle = { onToggleItem(item.itemId, false) },
                  onClick = { onEditItem(item) },
                  isChecked = true,
                )
              }
            }
          }
        }
      }
    }
  }

  if (state.editingItem != null) {
    ItemEditBottomSheet(
      item = state.editingItem,
      onSave = onUpdateItem,
      onDismiss = { onEditItem(null) },
    )
  }
}

@Composable
private fun QuickAddField(onAdd: (String) -> Unit, modifier: Modifier = Modifier) {
  var text by remember { mutableStateOf("") }
  OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    modifier = modifier.fillMaxWidth().padding(QuickAddPadding),
    placeholder = { Text("Add an item...") },
    singleLine = true,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions =
      KeyboardActions(
        onDone = {
          if (text.isNotBlank()) {
            onAdd(text)
            text = ""
          }
        }
      ),
    trailingIcon = {
      if (text.isNotBlank()) {
        IconButton(
          onClick = {
            onAdd(text)
            text = ""
          }
        ) {
          Icon(imageVector = DayKeeperIcons.Add, contentDescription = "Add item")
        }
      }
    },
  )
}

@Composable
private fun ShoppingListItemRow(
  item: ShoppingListItem,
  onToggle: () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isChecked: Boolean = false,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = ItemRowHorizontalPadding, vertical = ItemRowVerticalPadding),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(checked = isChecked, onCheckedChange = { onToggle() })
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = item.name,
        style = MaterialTheme.typography.bodyLarge,
        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
        color =
          if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant
          else MaterialTheme.colorScheme.onSurface,
      )
      if (item.quantity != ShoppingListItem.DEFAULT_QUANTITY || item.unit != null) {
        val qtyText = buildString {
          append(formatQuantity(item.quantity))
          if (item.unit != null) {
            append(" ")
            append(item.unit)
          }
        }
        Text(
          text = qtyText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun CheckedSectionHeader(
  count: Int,
  expanded: Boolean,
  onToggle: () -> Unit,
  onClear: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HorizontalDivider()
  Row(
    modifier = modifier.fillMaxWidth().clickable(onClick = onToggle).padding(CheckedHeaderPadding),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = if (expanded) DayKeeperIcons.ExpandLess else DayKeeperIcons.ExpandMore,
        contentDescription = if (expanded) "Collapse" else "Expand",
      )
      Text(
        text = "Checked ($count)",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    AnimatedVisibility(visible = expanded) {
      TextButton(onClick = onClear, modifier = Modifier.padding(start = ClearButtonPadding)) {
        Text("Clear all")
      }
    }
  }
}

private fun formatQuantity(quantity: Double): String {
  return if (quantity == quantity.toLong().toDouble()) {
    quantity.toLong().toString()
  } else {
    quantity.toString()
  }
}
