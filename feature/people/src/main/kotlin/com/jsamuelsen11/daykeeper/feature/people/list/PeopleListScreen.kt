package com.jsamuelsen11.daykeeper.feature.people.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperFloatingActionButton
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperSearchBar
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.component.SwipeableListItem
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.people.component.PersonCard
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private val ListContentPadding = 8.dp
private val FastScrollWidth = 24.dp
private const val FAST_SCROLL_LETTER_SIZE = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListScreen(
  onPersonClick: (String) -> Unit,
  onCreatePerson: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PeopleListViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var deleteTarget by remember { mutableStateOf<String?>(null) }

  DeletePersonDialog(deleteTarget = deleteTarget, onConfirm = viewModel::deletePerson) {
    deleteTarget = null
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      DayKeeperTopAppBar(title = "People") {
        SortMenu(
          currentSort =
            (uiState as? PeopleListUiState.Success)?.sortOrder ?: PeopleSortOrder.FIRST_NAME_ASC,
          onSortChanged = viewModel::onSortOrderChanged,
        )
      }
    },
    floatingActionButton = {
      DayKeeperFloatingActionButton(
        onClick = onCreatePerson,
        icon = DayKeeperIcons.Add,
        contentDescription = "Add person",
      )
    },
  ) { innerPadding ->
    val isRefreshing = (uiState as? PeopleListUiState.Success)?.isRefreshing ?: false
    PullToRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = viewModel::onRefresh,
      modifier = Modifier.padding(innerPadding),
    ) {
      PeopleListContent(
        uiState = uiState,
        onPersonClick = onPersonClick,
        onDeletePerson = { deleteTarget = it },
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
      )
    }
  }
}

@Composable
private fun SortMenu(currentSort: PeopleSortOrder, onSortChanged: (PeopleSortOrder) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  IconButton(onClick = { expanded = true }) {
    Icon(imageVector = DayKeeperIcons.Sort, contentDescription = "Sort")
  }
  DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
    DropdownMenuItem(
      text = { Text("First name") },
      onClick = {
        onSortChanged(PeopleSortOrder.FIRST_NAME_ASC)
        expanded = false
      },
      trailingIcon = {
        if (currentSort == PeopleSortOrder.FIRST_NAME_ASC) {
          Icon(imageVector = DayKeeperIcons.Check, contentDescription = null)
        }
      },
    )
    DropdownMenuItem(
      text = { Text("Last name") },
      onClick = {
        onSortChanged(PeopleSortOrder.LAST_NAME_ASC)
        expanded = false
      },
      trailingIcon = {
        if (currentSort == PeopleSortOrder.LAST_NAME_ASC) {
          Icon(imageVector = DayKeeperIcons.Check, contentDescription = null)
        }
      },
    )
  }
}

@Composable
private fun PeopleListContent(
  uiState: PeopleListUiState,
  onPersonClick: (String) -> Unit,
  onDeletePerson: (String) -> Unit,
  onSearchQueryChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    is PeopleListUiState.Loading -> LoadingIndicator(modifier = modifier.fillMaxSize())
    is PeopleListUiState.Error ->
      EmptyStateView(
        icon = DayKeeperIcons.People,
        title = "Something went wrong",
        body = uiState.message,
        modifier = modifier,
      )
    is PeopleListUiState.Success -> {
      Column(modifier = modifier.fillMaxSize()) {
        DayKeeperSearchBar(
          query = uiState.searchQuery,
          onQueryChange = onSearchQueryChanged,
          placeholder = "Search people",
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (uiState.people.isEmpty()) {
          EmptyStateView(
            icon = DayKeeperIcons.People,
            title = "No contacts yet",
            body = "Tap + to add your first contact.",
          )
        } else {
          PeopleListWithFastScroll(
            people = uiState.people,
            sortOrder = uiState.sortOrder,
            onPersonClick = onPersonClick,
            onDeletePerson = onDeletePerson,
          )
        }
      }
    }
  }
}

@Composable
private fun PeopleListWithFastScroll(
  people: List<PersonSummary>,
  sortOrder: PeopleSortOrder,
  onPersonClick: (String) -> Unit,
  onDeletePerson: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val groupedPeople = remember(people, sortOrder) { groupByLetter(people, sortOrder) }
  val letters = remember(groupedPeople) { groupedPeople.keys.toList() }
  val flatItems = remember(groupedPeople) { flattenGrouped(groupedPeople) }

  androidx.compose.foundation.layout.Row(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      state = listState,
      modifier = Modifier.weight(1f),
      contentPadding = PaddingValues(vertical = ListContentPadding),
    ) {
      items(items = flatItems, key = { it.key }) { item ->
        when (item) {
          is PeopleListItem.Header ->
            Text(
              text = item.letter,
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
          is PeopleListItem.PersonItem ->
            SwipeableListItem(onDelete = { onDeletePerson(item.summary.person.personId) }) {
              PersonCard(
                person = item.summary.person,
                primaryContactMethod = item.summary.primaryContactMethod,
                onClick = { onPersonClick(item.summary.person.personId) },
              )
            }
        }
      }
    }

    Column(
      modifier = Modifier.width(FastScrollWidth).fillMaxHeight().padding(vertical = 4.dp),
      verticalArrangement = Arrangement.SpaceEvenly,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      letters.forEach { letter ->
        Text(
          text = letter,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = FAST_SCROLL_LETTER_SIZE.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier =
            Modifier.clickable {
              val index =
                flatItems.indexOfFirst { it is PeopleListItem.Header && it.letter == letter }
              if (index >= 0) {
                coroutineScope.launch { listState.scrollToItem(index) }
              }
            },
        )
      }
    }
  }
}

@Composable
private fun DeletePersonDialog(
  deleteTarget: String?,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  if (deleteTarget != null) {
    ConfirmationDialog(
      title = "Delete contact?",
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

private sealed interface PeopleListItem {
  val key: String

  data class Header(val letter: String) : PeopleListItem {
    override val key = "header-$letter"
  }

  data class PersonItem(val summary: PersonSummary) : PeopleListItem {
    override val key = summary.person.personId
  }
}

private fun groupByLetter(
  people: List<PersonSummary>,
  sortOrder: PeopleSortOrder,
): Map<String, List<PersonSummary>> =
  people.groupBy { summary ->
    val name =
      when (sortOrder) {
        PeopleSortOrder.FIRST_NAME_ASC -> summary.person.firstName
        PeopleSortOrder.LAST_NAME_ASC -> summary.person.lastName
      }
    if (name.isNotBlank()) name.first().uppercaseChar().toString() else "#"
  }

private fun flattenGrouped(grouped: Map<String, List<PersonSummary>>): List<PeopleListItem> =
  buildList {
    grouped.forEach { (letter, people) ->
      add(PeopleListItem.Header(letter))
      people.forEach { add(PeopleListItem.PersonItem(it)) }
    }
  }
