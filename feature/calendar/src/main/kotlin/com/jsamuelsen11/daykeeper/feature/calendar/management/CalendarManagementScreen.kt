package com.jsamuelsen11.daykeeper.feature.calendar.management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperFloatingActionButton
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.component.SwipeableListItem
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.calendar.component.parseHexColor
import org.koin.compose.viewmodel.koinViewModel

private val ContentPadding = 16.dp
private val ItemSpacing = 8.dp
private val ColorDotSize = 12.dp
private val RowIconTextSpacing = 12.dp
private const val LABEL_CALENDARS = "Calendars"
private const val LABEL_CREATE_CALENDAR = "Create calendar"
private const val LABEL_DELETE_TITLE = "Delete Calendar"
private const val LABEL_DELETE_MESSAGE = "Are you sure you want to delete this calendar?"
private const val LABEL_EVENTS_SUFFIX = " events"
private const val LABEL_DEFAULT = "Default"

@Composable
fun CalendarManagementScreen(
  onNavigateBack: () -> Unit,
  onCreateCalendar: () -> Unit,
  onEditCalendar: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CalendarManagementViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var deleteTarget by remember { mutableStateOf<String?>(null) }

  if (deleteTarget != null) {
    ConfirmationDialog(
      title = LABEL_DELETE_TITLE,
      body = LABEL_DELETE_MESSAGE,
      onConfirm = {
        deleteTarget?.let { viewModel.deleteCalendar(it) }
        deleteTarget = null
      },
      onDismiss = { deleteTarget = null },
    )
  }

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = LABEL_CALENDARS, onNavigationClick = onNavigateBack) },
    floatingActionButton = {
      DayKeeperFloatingActionButton(
        onClick = onCreateCalendar,
        icon = DayKeeperIcons.Add,
        contentDescription = LABEL_CREATE_CALENDAR,
      )
    },
  ) { innerPadding ->
    when (val state = uiState) {
      is CalendarManagementUiState.Loading ->
        LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
      is CalendarManagementUiState.Error ->
        EmptyStateView(
          icon = DayKeeperIcons.Calendar,
          title = "Could not load calendars",
          body = state.message,
          modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
      is CalendarManagementUiState.Success ->
        if (state.items.isEmpty()) {
          EmptyStateView(
            icon = DayKeeperIcons.Calendar,
            title = "No calendars yet",
            body = "Tap the + button to create one.",
            modifier = Modifier.fillMaxSize().padding(innerPadding),
          )
        } else {
          CalendarList(
            items = state.items,
            onEditCalendar = onEditCalendar,
            onDeleteCalendar = { deleteTarget = it },
            modifier = Modifier.padding(innerPadding),
          )
        }
    }
  }
}

@Composable
private fun CalendarList(
  items: List<CalendarListItem>,
  onEditCalendar: (String) -> Unit,
  onDeleteCalendar: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(ContentPadding),
    verticalArrangement = Arrangement.spacedBy(ItemSpacing),
  ) {
    items(items = items, key = { it.calendar.calendarId }) { item ->
      SwipeableListItem(onDelete = { onDeleteCalendar(item.calendar.calendarId) }) {
        CalendarListRow(item = item, onClick = { onEditCalendar(item.calendar.calendarId) })
      }
    }
  }
}

@Composable
private fun CalendarListRow(
  item: CalendarListItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val color = remember(item.calendar.color) { parseHexColor(item.calendar.color) }

  Row(
    modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(ContentPadding),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier.size(ColorDotSize)
          .clip(CircleShape)
          .background(color ?: MaterialTheme.colorScheme.primary)
    )
    Spacer(modifier = Modifier.width(RowIconTextSpacing))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = item.calendar.name, style = MaterialTheme.typography.bodyLarge)
      Row(horizontalArrangement = Arrangement.spacedBy(ItemSpacing)) {
        Text(
          text = "${item.eventCount}$LABEL_EVENTS_SUFFIX",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (item.calendar.isDefault) {
          Text(
            text = LABEL_DEFAULT,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}
