package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.model.space.SpaceRole
import com.jsamuelsen11.daykeeper.core.model.space.SpaceType
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme
import com.jsamuelsen11.daykeeper.core.ui.theme.dayKeeperColors

private val BadgeIconSize = 16.dp
private val BadgeHorizontalPadding = 8.dp
private val BadgeVerticalPadding = 4.dp
private val BadgeContentSpacing = 4.dp
private const val BACKGROUND_ALPHA = 0.12f

@Composable
fun SpaceBadge(
  spaceName: String,
  spaceType: SpaceType,
  modifier: Modifier = Modifier,
  role: SpaceRole? = null,
) {
  val color = spaceTypeColor(spaceType)
  val description = buildString {
    append("$spaceName ${spaceType.name.lowercase()} space")
    if (role != null) append(", ${role.name.lowercase()}")
  }

  Surface(
    modifier = modifier.semantics { contentDescription = description },
    shape = MaterialTheme.shapes.small,
    color = color.copy(alpha = BACKGROUND_ALPHA),
  ) {
    Row(
      modifier =
        Modifier.padding(horizontal = BadgeHorizontalPadding, vertical = BadgeVerticalPadding),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(BadgeContentSpacing),
    ) {
      Icon(
        imageVector = spaceTypeIcon(spaceType),
        contentDescription = null,
        modifier = Modifier.size(BadgeIconSize),
        tint = color,
      )
      Text(text = spaceName, style = MaterialTheme.typography.labelSmall, color = color)
      if (role != null) {
        Icon(
          imageVector = roleIcon(role),
          contentDescription = null,
          modifier = Modifier.size(BadgeIconSize),
          tint = color,
        )
      }
    }
  }
}

@Composable
private fun spaceTypeColor(spaceType: SpaceType): Color =
  when (spaceType) {
    SpaceType.PERSONAL -> MaterialTheme.dayKeeperColors.spaceType.personal
    SpaceType.SHARED -> MaterialTheme.dayKeeperColors.spaceType.shared
    SpaceType.SYSTEM -> MaterialTheme.dayKeeperColors.spaceType.system
  }

private fun spaceTypeIcon(spaceType: SpaceType): ImageVector =
  when (spaceType) {
    SpaceType.PERSONAL -> DayKeeperIcons.Profile
    SpaceType.SHARED -> DayKeeperIcons.People
    SpaceType.SYSTEM -> DayKeeperIcons.Settings
  }

private fun roleIcon(role: SpaceRole): ImageVector =
  when (role) {
    SpaceRole.OWNER -> DayKeeperIcons.Profile
    SpaceRole.EDITOR -> DayKeeperIcons.Edit
    SpaceRole.VIEWER -> DayKeeperIcons.Visibility
  }

@Preview(showBackground = true)
@Composable
private fun SpaceBadgePersonalPreview() {
  DayKeeperTheme { SpaceBadge(spaceName = "Personal", spaceType = SpaceType.PERSONAL) }
}

@Preview(showBackground = true)
@Composable
private fun SpaceBadgeSharedWithRolePreview() {
  DayKeeperTheme {
    SpaceBadge(spaceName = "Family", spaceType = SpaceType.SHARED, role = SpaceRole.EDITOR)
  }
}

@Preview(showBackground = true)
@Composable
private fun SpaceBadgeSystemPreview() {
  DayKeeperTheme { SpaceBadge(spaceName = "Holidays", spaceType = SpaceType.SYSTEM) }
}
