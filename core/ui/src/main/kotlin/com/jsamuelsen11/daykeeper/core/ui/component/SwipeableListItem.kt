package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

val SwipeIconSize = 24.dp
val SwipeBackgroundPadding = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableListItem(
  modifier: Modifier = Modifier,
  onComplete: (() -> Unit)? = null,
  onDelete: (() -> Unit)? = null,
  content: @Composable RowScope.() -> Unit,
) {
  val dismissState = rememberSwipeToDismissBoxState()

  LaunchedEffect(dismissState.currentValue) {
    when (dismissState.currentValue) {
      SwipeToDismissBoxValue.StartToEnd -> {
        onComplete?.invoke()
        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
      }
      SwipeToDismissBoxValue.EndToStart -> {
        onDelete?.invoke()
        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
      }
      SwipeToDismissBoxValue.Settled -> Unit
    }
  }

  SwipeToDismissBox(
    state = dismissState,
    modifier = modifier,
    enableDismissFromStartToEnd = onComplete != null,
    enableDismissFromEndToStart = onDelete != null,
    backgroundContent = { SwipeBackground(dismissState.dismissDirection) },
  ) {
    Row(
      modifier = Modifier.background(MaterialTheme.colorScheme.surface),
      verticalAlignment = Alignment.CenterVertically,
      content = content,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(direction: SwipeToDismissBoxValue) {
  when (direction) {
    SwipeToDismissBoxValue.StartToEnd -> {
      Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.CenterStart,
      ) {
        Icon(
          imageVector = DayKeeperIcons.Check,
          contentDescription = "Complete",
          modifier = Modifier.padding(start = SwipeBackgroundPadding).size(SwipeIconSize),
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      }
    }
    SwipeToDismissBoxValue.EndToStart -> {
      Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.CenterEnd,
      ) {
        Icon(
          imageVector = DayKeeperIcons.Delete,
          contentDescription = "Delete",
          modifier = Modifier.padding(end = SwipeBackgroundPadding).size(SwipeIconSize),
          tint = MaterialTheme.colorScheme.onErrorContainer,
        )
      }
    }
    SwipeToDismissBoxValue.Settled -> {
      Box(modifier = Modifier.fillMaxSize())
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun SwipeableListItemBothPreview() {
  DayKeeperTheme {
    SwipeableListItem(onComplete = {}, onDelete = {}) {
      Text(
        text = "Buy groceries",
        modifier = Modifier.padding(horizontal = SwipeBackgroundPadding, vertical = SwipeIconSize),
        style = MaterialTheme.typography.bodyLarge,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun SwipeableListItemDeleteOnlyPreview() {
  DayKeeperTheme {
    SwipeableListItem(onDelete = {}) {
      Text(
        text = "Shopping list item",
        modifier = Modifier.padding(horizontal = SwipeBackgroundPadding, vertical = SwipeIconSize),
        style = MaterialTheme.typography.bodyLarge,
      )
    }
  }
}
