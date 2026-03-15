package com.jsamuelsen11.daykeeper.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jsamuelsen11.daykeeper.app.R
import com.jsamuelsen11.daykeeper.core.ui.component.EmptyStateView

@Composable
fun ComingSoonScreen(destination: TopLevelDestination, modifier: Modifier = Modifier) {
  EmptyStateView(
    icon = destination.icon,
    title = stringResource(R.string.coming_soon_title),
    body = stringResource(R.string.coming_soon_body),
    modifier = modifier,
  )
}
