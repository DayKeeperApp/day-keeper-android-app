package com.jsamuelsen11.daykeeper.feature.profile.space

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.model.space.SpaceRole
import com.jsamuelsen11.daykeeper.core.model.space.SpaceType
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperFloatingActionButton
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.component.SpaceBadge
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import org.koin.compose.viewmodel.koinViewModel

private val ContentPadding = 16.dp
private val ItemVerticalPadding = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceManagementScreen(
  onNavigateBack: () -> Unit,
  onSpaceClick: (String) -> Unit,
  onCreateSpace: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SpaceManagementViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = "Spaces", onNavigationClick = onNavigateBack) },
    floatingActionButton = {
      DayKeeperFloatingActionButton(
        onClick = onCreateSpace,
        icon = DayKeeperIcons.Add,
        contentDescription = "Create space",
      )
    },
  ) { padding ->
    when (val state = uiState) {
      is SpaceManagementUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(padding))
      is SpaceManagementUiState.Error ->
        Text(
          text = state.message,
          modifier = Modifier.padding(padding).padding(ContentPadding),
          color = MaterialTheme.colorScheme.error,
        )
      is SpaceManagementUiState.Success ->
        SpaceList(
          groupedSpaces = state.groupedSpaces,
          onSpaceClick = onSpaceClick,
          modifier = Modifier.padding(padding),
        )
    }
  }
}

@Composable
private fun SpaceList(
  groupedSpaces: Map<SpaceType, List<SpaceWithMeta>>,
  onSpaceClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(modifier = modifier.fillMaxSize()) {
    SpaceType.entries.forEach { type ->
      val spaces = groupedSpaces[type].orEmpty()
      if (spaces.isNotEmpty()) {
        item {
          Text(
            text = spaceTypeLabel(type),
            modifier = Modifier.padding(horizontal = ContentPadding, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
          )
        }
        items(spaces, key = { it.space.spaceId }) { spaceWithMeta ->
          SpaceRow(
            spaceWithMeta = spaceWithMeta,
            onClick = { onSpaceClick(spaceWithMeta.space.spaceId) },
          )
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
      }
    }
  }
}

@Composable
private fun SpaceRow(
  spaceWithMeta: SpaceWithMeta,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = ContentPadding, vertical = ItemVerticalPadding),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = spaceWithMeta.space.name, style = MaterialTheme.typography.bodyLarge)
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SpaceBadge(spaceName = spaceWithMeta.space.name, spaceType = spaceWithMeta.space.type)
        Text(
          text =
            "${spaceWithMeta.memberCount} member${if (spaceWithMeta.memberCount != 1) "s" else ""}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    spaceWithMeta.userRole?.let { role ->
      Text(
        text = roleLabel(role),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

private fun spaceTypeLabel(type: SpaceType): String =
  when (type) {
    SpaceType.PERSONAL -> "Personal"
    SpaceType.SHARED -> "Shared"
    SpaceType.SYSTEM -> "System"
  }

private fun roleLabel(role: SpaceRole): String =
  when (role) {
    SpaceRole.OWNER -> "Owner"
    SpaceRole.EDITOR -> "Editor"
    SpaceRole.VIEWER -> "Viewer"
  }
