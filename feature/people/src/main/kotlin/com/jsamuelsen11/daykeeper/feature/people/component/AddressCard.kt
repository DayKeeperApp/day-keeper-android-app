package com.jsamuelsen11.daykeeper.feature.people.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.people.Address
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons

@Composable
fun AddressCard(address: Address, modifier: Modifier = Modifier, onTap: (() -> Unit)? = null) {
  val rowModifier =
    if (onTap != null)
      modifier
        .fillMaxWidth()
        .clickable(onClick = onTap)
        .padding(horizontal = 16.dp, vertical = 8.dp)
    else modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)

  Row(modifier = rowModifier, verticalAlignment = Alignment.Top) {
    Icon(
      imageVector = DayKeeperIcons.Map,
      contentDescription = "Address",
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      if (address.label.isNotBlank()) {
        Text(
          text = address.label,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Text(text = formatAddress(address), style = MaterialTheme.typography.bodyMedium)
    }
  }
}

internal fun formatAddress(address: Address): String =
  buildString {
      address.street?.let { appendLine(it) }
      val cityStateLine = listOfNotNull(address.city, address.state).joinToString(", ")
      val postal = address.postalCode.orEmpty()
      val cityStatePostal =
        if (postal.isNotEmpty() && cityStateLine.isNotEmpty()) "$cityStateLine $postal"
        else if (postal.isNotEmpty()) postal else cityStateLine
      if (cityStatePostal.isNotEmpty()) appendLine(cityStatePostal)
      address.country?.let { append(it) }
    }
    .trimEnd()
