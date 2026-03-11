package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

private val FilterChipRowVerticalPadding = 8.dp
private val FilterChipSpacing = 8.dp
private val ClearIconSize = 20.dp
private val PreviewBlue = Color(0xFF2196F3)
private val PreviewGreen = Color(0xFF4CAF50)

@Composable
fun DayKeeperSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "Search",
  onSearch: ((String) -> Unit)? = null,
  filterChips: @Composable RowScope.() -> Unit = {},
) {
  Column(modifier = modifier) {
    OutlinedTextField(
      value = query,
      onValueChange = onQueryChange,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text(text = placeholder) },
      leadingIcon = { Icon(imageVector = DayKeeperIcons.Search, contentDescription = null) },
      trailingIcon = {
        if (query.isNotEmpty()) {
          IconButton(onClick = { onQueryChange("") }) {
            Icon(
              imageVector = DayKeeperIcons.Close,
              contentDescription = "Clear search",
              modifier = Modifier.size(ClearIconSize),
            )
          }
        }
      },
      singleLine = true,
      shape = MaterialTheme.shapes.medium,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke(query) }),
    )
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(vertical = FilterChipRowVerticalPadding),
      horizontalArrangement = Arrangement.spacedBy(FilterChipSpacing),
      content = filterChips,
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun DayKeeperSearchBarEmptyPreview() {
  DayKeeperTheme { DayKeeperSearchBar(query = "", onQueryChange = {}) }
}

@Preview(showBackground = true)
@Composable
private fun DayKeeperSearchBarWithQueryPreview() {
  DayKeeperTheme { DayKeeperSearchBar(query = "groceries", onQueryChange = {}) }
}

@Preview(showBackground = true)
@Composable
private fun DayKeeperSearchBarWithFiltersPreview() {
  DayKeeperTheme {
    DayKeeperSearchBar(query = "tasks", onQueryChange = {}) {
      CategoryChip(name = "Work", color = PreviewBlue, selected = true, onClick = {})
      CategoryChip(name = "Personal", onClick = {})
      CategoryChip(name = "Health", color = PreviewGreen, onClick = {})
    }
  }
}
