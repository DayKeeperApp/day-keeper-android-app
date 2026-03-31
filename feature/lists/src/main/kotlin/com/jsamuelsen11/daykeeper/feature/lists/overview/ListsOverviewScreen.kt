package com.jsamuelsen11.daykeeper.feature.lists.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private val CardPadding = 16.dp
private val ListSpacing = 8.dp
private val ListContentPadding = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsOverviewScreen(
  onListClick: (String) -> Unit,
  onCreateList: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ListsOverviewViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var deleteTarget by remember { mutableStateOf<String?>(null) }

  DeleteListDialog(deleteTarget = deleteTarget, onConfirm = viewModel::deleteList) {
    deleteTarget = null
  }

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = "Lists") },
    floatingActionButton = {
      DayKeeperFloatingActionButton(
        onClick = onCreateList,
        icon = DayKeeperIcons.Add,
        contentDescription = "Create list",
      )
    },
  ) { innerPadding ->
    val isRefreshing = (uiState as? ListsOverviewUiState.Success)?.isRefreshing ?: false
    PullToRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = viewModel::onRefresh,
      modifier = Modifier.padding(innerPadding),
    ) {
      ListsOverviewContent(
        uiState = uiState,
        onListClick = onListClick,
        onDeleteList = { deleteTarget = it },
      )
    }
  }
}

@Composable
private fun DeleteListDialog(
  deleteTarget: String?,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  if (deleteTarget != null) {
    ConfirmationDialog(
      title = "Delete list?",
      body = "This action cannot be undone.",
      icon = DayKeeperIcons.Delete,
      confirmLabel = "Delete",
      dismissLabel = "Cancel",
      onConfirm = {
        onConfirm(deleteTarget)
        onDismiss()
      },
      onDismiss = onDismiss,
    )
  }
}

@Composable
private fun ListsOverviewContent(
  uiState: ListsOverviewUiState,
  onListClick: (String) -> Unit,
  onDeleteList: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    is ListsOverviewUiState.Loading -> LoadingIndicator(modifier = modifier.fillMaxSize())
    is ListsOverviewUiState.Error ->
      EmptyStateView(
        icon = DayKeeperIcons.Lists,
        title = "Something went wrong",
        body = uiState.message,
        modifier = modifier,
      )
    is ListsOverviewUiState.Success -> {
      if (uiState.lists.isEmpty()) {
        EmptyStateView(
          icon = DayKeeperIcons.Lists,
          title = "No lists yet",
          body = "Tap + to create your first list.",
          modifier = modifier,
        )
      } else {
        LazyColumn(
          modifier = modifier.fillMaxSize(),
          contentPadding = PaddingValues(ListContentPadding),
          verticalArrangement = Arrangement.spacedBy(ListSpacing),
        ) {
          items(items = uiState.lists, key = { it.list.listId }) { summary ->
            SwipeableListItem(onDelete = { onDeleteList(summary.list.listId) }) {
              ShoppingListCard(summary = summary, onClick = { onListClick(summary.list.listId) })
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ShoppingListCard(
  summary: ShoppingListSummary,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(CardPadding),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(text = summary.list.name, style = MaterialTheme.typography.titleMedium)
        val remaining = summary.totalItems - summary.checkedItems
        val itemText =
          if (summary.totalItems == 0) "No items"
          else "$remaining of ${summary.totalItems} remaining"
        Text(
          text = itemText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
