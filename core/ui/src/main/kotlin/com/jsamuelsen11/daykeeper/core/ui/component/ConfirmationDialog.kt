package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

@Composable
fun ConfirmationDialog(
  title: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  body: String? = null,
  icon: ImageVector? = null,
  confirmLabel: String = ConfirmationDialogDefaults.CONFIRM_LABEL,
  dismissLabel: String = ConfirmationDialogDefaults.DISMISS_LABEL,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
    modifier = modifier,
    dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
    icon = icon?.let { { Icon(imageVector = it, contentDescription = null) } },
    title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
    text = body?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
  )
}

object ConfirmationDialogDefaults {
  const val CONFIRM_LABEL = "Confirm"
  const val DISMISS_LABEL = "Cancel"
}

@Preview(showBackground = true)
@Composable
private fun ConfirmationDialogMinimalPreview() {
  DayKeeperTheme { ConfirmationDialog(title = "Are you sure?", onConfirm = {}, onDismiss = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmationDialogFullPreview() {
  DayKeeperTheme {
    ConfirmationDialog(
      title = "Delete task?",
      onConfirm = {},
      onDismiss = {},
      body = "This action cannot be undone.",
      icon = DayKeeperIcons.Delete,
      confirmLabel = "Delete",
      dismissLabel = "Keep",
    )
  }
}
