package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayKeeperTopAppBar(
  title: String,
  modifier: Modifier = Modifier,
  onNavigationClick: (() -> Unit)? = null,
  actions: @Composable RowScope.() -> Unit = {},
) {
  CenterAlignedTopAppBar(
    title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    modifier = modifier,
    navigationIcon = {
      if (onNavigationClick != null) {
        IconButton(onClick = onNavigationClick) {
          Icon(imageVector = DayKeeperIcons.ArrowBack, contentDescription = "Navigate back")
        }
      }
    },
    actions = actions,
  )
}

@Preview(showBackground = true)
@Composable
private fun DayKeeperTopAppBarMainPreview() {
  DayKeeperTheme { DayKeeperTopAppBar(title = "Day Keeper") }
}

@Preview(showBackground = true)
@Composable
private fun DayKeeperTopAppBarBackPreview() {
  DayKeeperTheme { DayKeeperTopAppBar(title = "Details", onNavigationClick = {}) }
}
