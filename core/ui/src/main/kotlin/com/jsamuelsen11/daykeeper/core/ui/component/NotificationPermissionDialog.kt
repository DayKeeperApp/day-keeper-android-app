package com.jsamuelsen11.daykeeper.core.ui.component

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Shows a rationale dialog and requests POST_NOTIFICATIONS permission on Android 13+. Does nothing
 * on Android 12 and below.
 *
 * @param onPermissionResult Called with `true` if granted, `false` if denied.
 * @param onDismiss Called when the user dismisses the rationale dialog without granting.
 */
@Composable
fun NotificationPermissionDialog(
  onPermissionResult: (Boolean) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
    LaunchedEffect(Unit) { onPermissionResult(true) }
    return
  }

  var showRationale by remember { mutableStateOf(true) }

  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      onPermissionResult(granted)
    }

  if (showRationale) {
    AlertDialog(
      modifier = modifier,
      onDismissRequest = onDismiss,
      title = { Text("Enable Notifications") },
      text = {
        Text(
          "Day Keeper needs notification permission to send you reminders " +
            "for upcoming events and tasks. Without this, reminders will not appear."
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showRationale = false
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
        ) {
          Text("Allow")
        }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Not Now") } },
    )
  }
}
