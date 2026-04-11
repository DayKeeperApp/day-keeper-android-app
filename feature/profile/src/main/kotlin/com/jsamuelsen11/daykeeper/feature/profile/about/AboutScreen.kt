package com.jsamuelsen11.daykeeper.feature.profile.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperTopAppBar

private val ContentPadding = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
  onNavigateBack: () -> Unit,
  onOpenLicenses: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

  Scaffold(
    modifier = modifier,
    topBar = { DayKeeperTopAppBar(title = "About", onNavigationClick = onNavigateBack) },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(ContentPadding),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Spacer(modifier = Modifier.height(ContentPadding))

      Text(text = "Day Keeper", style = MaterialTheme.typography.headlineMedium)

      Text(
        text = "Version ${packageInfo.versionName}",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      @Suppress("DEPRECATION")
      Text(
        text = "Build ${packageInfo.versionCode}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(modifier = Modifier.height(ContentPadding))
      HorizontalDivider()
      Spacer(modifier = Modifier.height(ContentPadding))

      OutlinedButton(onClick = onOpenLicenses, modifier = Modifier.fillMaxWidth()) {
        Text("Open Source Licenses")
      }
    }
  }
}
