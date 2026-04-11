package com.jsamuelsen11.daykeeper.feature.profile.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import org.koin.compose.viewmodel.koinViewModel

private val AvatarSize = 80.dp
private val ContentPadding = 16.dp
private val ItemVerticalPadding = 14.dp

@Composable
fun ProfileOverviewScreen(
  onAccountSettingsClick: () -> Unit,
  onSpacesClick: () -> Unit,
  onDevicesClick: () -> Unit,
  onSyncStatusClick: () -> Unit,
  onStorageClick: () -> Unit,
  onAboutClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProfileOverviewViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(modifier = modifier, topBar = { DayKeeperTopAppBar(title = "Profile") }) { padding ->
    when (val state = uiState) {
      is ProfileOverviewUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(padding))
      is ProfileOverviewUiState.Error ->
        ErrorContent(message = state.message, modifier = Modifier.padding(padding))
      is ProfileOverviewUiState.Success ->
        ProfileContent(
          state = state,
          menuItems =
            buildProfileMenuItems(
              onAccountSettingsClick = onAccountSettingsClick,
              onSpacesClick = onSpacesClick,
              onDevicesClick = onDevicesClick,
              onSyncStatusClick = onSyncStatusClick,
              onStorageClick = onStorageClick,
              onAboutClick = onAboutClick,
            ),
          modifier = Modifier.padding(padding),
        )
    }
  }
}

@Composable
private fun ProfileContent(
  state: ProfileOverviewUiState.Success,
  menuItems: List<ProfileMenuItem>,
  modifier: Modifier = Modifier,
) {
  LazyColumn(modifier = modifier.fillMaxSize()) {
    item { ProfileHeader(displayName = state.displayName, email = state.email) }
    item { Spacer(modifier = Modifier.height(ContentPadding)) }
    items(menuItems) { item ->
      ProfileMenuRow(item = item)
      HorizontalDivider()
    }
  }
}

@Composable
private fun ProfileHeader(displayName: String, email: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth().padding(ContentPadding),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    AvatarCircle(displayName = displayName)
    Text(text = displayName, style = MaterialTheme.typography.headlineSmall)
    if (email.isNotBlank()) {
      Text(
        text = email,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun AvatarCircle(displayName: String, modifier: Modifier = Modifier) {
  val initials =
    displayName
      .split(" ")
      .take(2)
      .mapNotNull { it.firstOrNull()?.uppercase() }
      .joinToString("")
      .ifEmpty { "?" }

  Box(
    modifier =
      modifier
        .size(AvatarSize)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = initials,
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.onPrimaryContainer,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun ProfileMenuRow(item: ProfileMenuItem, modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = item.onClick)
        .padding(horizontal = ContentPadding, vertical = ItemVerticalPadding),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = item.icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = item.label,
      modifier = Modifier.weight(1f).padding(horizontal = ContentPadding),
      style = MaterialTheme.typography.bodyLarge,
    )
    Icon(
      imageVector = DayKeeperIcons.ChevronRight,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun ErrorContent(message: String, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = message, color = MaterialTheme.colorScheme.error)
  }
}

private data class ProfileMenuItem(
  val icon: ImageVector,
  val label: String,
  val onClick: () -> Unit,
)

private fun buildProfileMenuItems(
  onAccountSettingsClick: () -> Unit,
  onSpacesClick: () -> Unit,
  onDevicesClick: () -> Unit,
  onSyncStatusClick: () -> Unit,
  onStorageClick: () -> Unit,
  onAboutClick: () -> Unit,
): List<ProfileMenuItem> =
  listOf(
    ProfileMenuItem(DayKeeperIcons.Settings, "Account Settings", onAccountSettingsClick),
    ProfileMenuItem(DayKeeperIcons.People, "Spaces", onSpacesClick),
    ProfileMenuItem(DayKeeperIcons.Devices, "Devices", onDevicesClick),
    ProfileMenuItem(DayKeeperIcons.Sync, "Sync Status", onSyncStatusClick),
    ProfileMenuItem(DayKeeperIcons.StorageIcon, "Storage", onStorageClick),
    ProfileMenuItem(DayKeeperIcons.Info, "About", onAboutClick),
  )
