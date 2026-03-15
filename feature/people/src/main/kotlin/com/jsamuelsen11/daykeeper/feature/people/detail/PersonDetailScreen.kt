package com.jsamuelsen11.daykeeper.feature.people.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsamuelsen11.daykeeper.core.model.people.Address
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethod
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethodType
import com.jsamuelsen11.daykeeper.core.ui.component.ConfirmationDialog
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.component.LoadingIndicator
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.feature.people.component.AddressCard
import com.jsamuelsen11.daykeeper.feature.people.component.ContactMethodRow
import com.jsamuelsen11.daykeeper.feature.people.component.ImportantDateRow
import com.jsamuelsen11.daykeeper.feature.people.component.PersonAvatar
import com.jsamuelsen11.daykeeper.feature.people.component.formatAddress
import org.koin.compose.viewmodel.koinViewModel

private val LargeAvatarSize = 80.dp
private val SectionSpacing = 16.dp

@Composable
fun PersonDetailScreen(
  onNavigateBack: () -> Unit,
  onEditPerson: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PersonDetailViewModel = koinViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showDeleteDialog by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    viewModel.events.collect { if (it is PersonDetailEvent.Deleted) onNavigateBack() }
  }

  if (showDeleteDialog) {
    ConfirmationDialog(
      title = "Delete contact?",
      body = "This will permanently delete this contact and all their information.",
      icon = DayKeeperIcons.Delete,
      confirmLabel = "Delete",
      dismissLabel = "Cancel",
      onConfirm = {
        viewModel.deletePerson()
        showDeleteDialog = false
      },
      onDismiss = { showDeleteDialog = false },
    )
  }

  val title =
    when (val state = uiState) {
      is PersonDetailUiState.Success -> "${state.person.firstName} ${state.person.lastName}"
      else -> "Person"
    }

  Scaffold(
    modifier = modifier,
    topBar = {
      DayKeeperTopAppBar(title = title, onNavigationClick = onNavigateBack) {
        IconButton(onClick = onEditPerson) {
          Icon(imageVector = DayKeeperIcons.Edit, contentDescription = "Edit")
        }
        OverflowMenu(onDelete = { showDeleteDialog = true })
      }
    },
  ) { innerPadding ->
    PersonDetailContent(uiState = uiState, modifier = Modifier.padding(innerPadding))
  }
}

@Composable
private fun OverflowMenu(onDelete: () -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  IconButton(onClick = { expanded = true }) {
    Icon(imageVector = DayKeeperIcons.MoreVert, contentDescription = "More options")
  }
  DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
    DropdownMenuItem(
      text = { Text("Delete") },
      onClick = {
        onDelete()
        expanded = false
      },
      leadingIcon = { Icon(imageVector = DayKeeperIcons.Delete, contentDescription = null) },
    )
  }
}

@Composable
private fun PersonDetailContent(uiState: PersonDetailUiState, modifier: Modifier = Modifier) {
  when (uiState) {
    is PersonDetailUiState.Loading -> LoadingIndicator(modifier = modifier.fillMaxSize())
    is PersonDetailUiState.Error ->
      EmptyStateView(
        icon = DayKeeperIcons.People,
        title = "Something went wrong",
        body = uiState.message,
        modifier = modifier,
      )
    is PersonDetailUiState.Success -> {
      val context = LocalContext.current
      Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PersonHeader(
          firstName = uiState.person.firstName,
          lastName = uiState.person.lastName,
          nickname = uiState.person.nickname,
        )

        uiState.person.notes?.let { notes ->
          if (notes.isNotBlank()) {
            SectionHeader("Notes")
            Text(
              text = notes,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(SectionSpacing))
          }
        }

        if (uiState.contactMethods.isNotEmpty()) {
          SectionHeader("Contact Methods")
          uiState.contactMethods.forEach { cm ->
            ContactMethodRow(contactMethod = cm, onTap = { launchContactIntent(context, cm) })
          }
          Spacer(modifier = Modifier.height(SectionSpacing))
        }

        if (uiState.addresses.isNotEmpty()) {
          SectionHeader("Addresses")
          uiState.addresses.forEach { address ->
            AddressCard(address = address, onTap = { launchMapIntent(context, address) })
          }
          Spacer(modifier = Modifier.height(SectionSpacing))
        }

        if (uiState.importantDates.isNotEmpty()) {
          SectionHeader("Important Dates")
          uiState.importantDates.forEach { date -> ImportantDateRow(importantDate = date) }
          Spacer(modifier = Modifier.height(SectionSpacing))
        }
      }
    }
  }
}

@Composable
private fun PersonHeader(firstName: String, lastName: String, nickname: String?) {
  Column(
    modifier = Modifier.padding(SectionSpacing),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    PersonAvatar(firstName = firstName, lastName = lastName, size = LargeAvatarSize)
    Spacer(modifier = Modifier.height(12.dp))
    Text(text = "$firstName $lastName", style = MaterialTheme.typography.headlineSmall)
    if (!nickname.isNullOrBlank()) {
      Text(
        text = nickname,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
  HorizontalDivider()
}

@Composable
private fun SectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
  )
}

private fun launchContactIntent(context: android.content.Context, cm: ContactMethod) {
  val intent =
    when (cm.type) {
      ContactMethodType.PHONE -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cm.value}"))
      ContactMethodType.EMAIL -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${cm.value}"))
    }
  runCatching { context.startActivity(intent) }
}

private fun launchMapIntent(context: android.content.Context, address: Address) {
  val query = Uri.encode(formatAddress(address))
  val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query"))
  runCatching { context.startActivity(intent) }
}
