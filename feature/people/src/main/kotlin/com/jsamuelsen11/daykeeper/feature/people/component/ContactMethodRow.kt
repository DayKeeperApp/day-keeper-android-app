package com.jsamuelsen11.daykeeper.feature.people.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethod
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethodType
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons

@Composable
fun ContactMethodRow(
  contactMethod: ContactMethod,
  modifier: Modifier = Modifier,
  onTap: (() -> Unit)? = null,
  onDelete: (() -> Unit)? = null,
) {
  val icon =
    when (contactMethod.type) {
      ContactMethodType.PHONE -> DayKeeperIcons.Phone
      ContactMethodType.EMAIL -> DayKeeperIcons.Email
    }
  val rowModifier =
    if (onTap != null)
      modifier
        .fillMaxWidth()
        .clickable(onClick = onTap)
        .padding(horizontal = 16.dp, vertical = 8.dp)
    else modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)

  Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = icon,
      contentDescription = contactMethod.type.name,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = contactMethod.value, style = MaterialTheme.typography.bodyLarge)
      if (contactMethod.label.isNotBlank()) {
        Text(
          text = contactMethod.label,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    if (onDelete != null) {
      IconButton(onClick = onDelete) {
        Icon(
          imageVector = DayKeeperIcons.Delete,
          contentDescription = "Remove",
          tint = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}
