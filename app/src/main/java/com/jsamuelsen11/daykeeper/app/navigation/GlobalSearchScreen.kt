package com.jsamuelsen11.daykeeper.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.app.R
import com.jsamuelsen11.daykeeper.core.ui.component.DayKeeperSearchBar
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons

private val SearchBarPadding = 16.dp

@Composable
fun GlobalSearchScreen(modifier: Modifier = Modifier) {
  var query by rememberSaveable { mutableStateOf("") }

  Column(modifier = modifier.fillMaxSize()) {
    DayKeeperSearchBar(
      query = query,
      onQueryChange = { query = it },
      placeholder = stringResource(R.string.global_search_placeholder),
      modifier = Modifier.padding(horizontal = SearchBarPadding),
    )
    EmptyStateView(
      icon = DayKeeperIcons.Search,
      title = stringResource(R.string.coming_soon_title),
      body = stringResource(R.string.coming_soon_body),
    )
  }
}
