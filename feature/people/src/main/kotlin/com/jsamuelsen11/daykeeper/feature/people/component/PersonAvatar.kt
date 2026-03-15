package com.jsamuelsen11.daykeeper.feature.people.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

private val AvatarColors =
  listOf(
    Color(0xFF1E88E5),
    Color(0xFF43A047),
    Color(0xFFE53935),
    Color(0xFF8E24AA),
    Color(0xFFFB8C00),
    Color(0xFF00ACC1),
    Color(0xFF3949AB),
    Color(0xFF7CB342),
  )

private const val DEFAULT_AVATAR_SIZE = 40

@Composable
fun PersonAvatar(
  firstName: String,
  lastName: String,
  modifier: Modifier = Modifier,
  size: Dp = DEFAULT_AVATAR_SIZE.dp,
) {
  val initials = buildString {
    if (firstName.isNotBlank()) append(firstName.first().uppercaseChar())
    if (lastName.isNotBlank()) append(lastName.first().uppercaseChar())
  }
  val colorIndex = "$firstName $lastName".hashCode().absoluteValue % AvatarColors.size
  val backgroundColor = AvatarColors[colorIndex]
  val textStyle =
    if (size >= DEFAULT_AVATAR_SIZE.dp * 2) MaterialTheme.typography.headlineMedium
    else MaterialTheme.typography.titleSmall

  Box(
    modifier = modifier.size(size).clip(CircleShape).background(backgroundColor),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = initials, color = Color.White, style = textStyle)
  }
}
