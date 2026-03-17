package com.jsamuelsen11.daykeeper.feature.tasks.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperFloatingActionButton
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.component.SwipeableListItem
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import org.koin.compose.viewmodel.koinViewModel

private val ColorSwatchSize = 24.dp
private val RowPadding = 16.dp
private val RowSpacing = 12.dp

@Composable
fun CategoryManagementScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CategoryManagementViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showAddDialog by remember { mutableStateOf(false) }
  var editTarget by remember { mutableStateOf<CategoryItem?>(null) }
  var deleteTarget by remember { mutableStateOf<CategoryItem?>(null) }

  Scaffold(
    modifier = modifier,
    topBar = {
      DayKeeperTopAppBar(title = "Manage Categories", onNavigationClick = onNavigateBack)
    },
    floatingActionButton = {
      DayKeeperFloatingActionButton(
        onClick = { showAddDialog = true },
        icon = DayKeeperIcons.Add,
        contentDescription = "Add category",
      )
    },
  ) { innerPadding ->
    CategoryManagementContent(
      uiState = uiState,
      onEdit = { editTarget = it },
      onDelete = { deleteTarget = it },
      modifier = Modifier.padding(innerPadding),
    )
  }

  CategoryManagementDialogs(
    showAddDialog = showAddDialog,
    editTarget = editTarget,
    deleteTarget = deleteTarget,
    onAddSave = { name, color ->
      viewModel.createCategory(name, color)
      showAddDialog = false
    },
    onAddDismiss = { showAddDialog = false },
    onEditSave = { item, name, color ->
      viewModel.updateCategory(item.category.categoryId, name, color)
      editTarget = null
    },
    onEditDismiss = { editTarget = null },
    onDeleteConfirm = { item ->
      viewModel.deleteCategory(item.category.categoryId)
      deleteTarget = null
    },
    onDeleteDismiss = { deleteTarget = null },
  )
}

@Composable
private fun CategoryManagementContent(
  uiState: CategoryManagementUiState,
  onEdit: (CategoryItem) -> Unit,
  onDelete: (CategoryItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (val state = uiState) {
    is CategoryManagementUiState.Loading -> LoadingIndicator(modifier = modifier.fillMaxSize())
    is CategoryManagementUiState.Error ->
      EmptyStateView(
        icon = DayKeeperIcons.Label,
        title = "Error loading categories",
        body = state.message,
        modifier = modifier,
      )
    is CategoryManagementUiState.Success ->
      CategoryList(
        categories = state.categories,
        onEdit = onEdit,
        onDelete = onDelete,
        modifier = modifier,
      )
  }
}

@Composable
private fun CategoryManagementDialogs(
  showAddDialog: Boolean,
  editTarget: CategoryItem?,
  deleteTarget: CategoryItem?,
  onAddSave: (String, String?) -> Unit,
  onAddDismiss: () -> Unit,
  onEditSave: (CategoryItem, String, String?) -> Unit,
  onEditDismiss: () -> Unit,
  onDeleteConfirm: (CategoryItem) -> Unit,
  onDeleteDismiss: () -> Unit,
) {
  if (showAddDialog) {
    CategoryEditDialog(onSave = onAddSave, onDismiss = onAddDismiss)
  }

  editTarget?.let { item ->
    CategoryEditDialog(
      onSave = { name, color -> onEditSave(item, name, color) },
      onDismiss = onEditDismiss,
      initialName = item.category.name,
      initialColor = item.category.color,
    )
  }

  deleteTarget?.let { item ->
    ConfirmationDialog(
      title = "Delete \"${item.category.name}\"?",
      body = "Tasks using this category will become uncategorized.",
      onConfirm = { onDeleteConfirm(item) },
      onDismiss = onDeleteDismiss,
    )
  }
}

@Composable
private fun CategoryList(
  categories: List<CategoryItem>,
  onEdit: (CategoryItem) -> Unit,
  onDelete: (CategoryItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (categories.isEmpty()) {
    EmptyStateView(
      icon = DayKeeperIcons.Label,
      title = "No categories yet",
      body = "Tap + to create your first category.",
      modifier = modifier.fillMaxSize(),
    )
    return
  }

  LazyColumn(modifier = modifier.fillMaxSize()) {
    items(items = categories, key = { it.category.categoryId }) { item ->
      if (item.category.isSystem) {
        CategoryRow(
          item = item,
          showLock = true,
          onClick = {},
          modifier = Modifier.padding(horizontal = RowPadding),
        )
      } else {
        SwipeableListItem(onDelete = { onDelete(item) }) {
          CategoryRow(item = item, showLock = false, onClick = { onEdit(item) })
        }
      }
    }
  }
}

@Composable
private fun CategoryRow(
  item: CategoryItem,
  showLock: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(RowPadding),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RowSpacing),
  ) {
    val swatchColor = item.category.color?.let { parseHexColor(it) }
    Box(
      modifier =
        Modifier.size(ColorSwatchSize)
          .clip(CircleShape)
          .background(swatchColor ?: MaterialTheme.colorScheme.surfaceVariant)
    )
    Text(
      text = item.category.name,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.weight(1f),
    )
    if (showLock) {
      Icon(
        imageVector = DayKeeperIcons.Visibility,
        contentDescription = "System category",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Text(
      text = "${item.taskCount}",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

private fun parseHexColor(hex: String): Color? =
  runCatching {
      val cleaned = hex.trimStart('#')
      val argb =
        when (cleaned.length) {
          HEX_RGB_LENGTH -> "FF$cleaned".toLong(radix = HEX_RADIX)
          HEX_ARGB_LENGTH -> cleaned.toLong(radix = HEX_RADIX)
          else -> return@runCatching null
        }
      Color(argb.toInt())
    }
    .getOrNull()

private const val HEX_RGB_LENGTH = 6
private const val HEX_ARGB_LENGTH = 8
private const val HEX_RADIX = 16
