package com.jsamuelsen11.daykeeper.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable data class CalendarColors(val palette: List<Color>)

@Immutable
data class PriorityColors(
  val none: Color,
  val low: Color,
  val medium: Color,
  val high: Color,
  val urgent: Color,
)

@Immutable data class SpaceTypeColors(val personal: Color, val shared: Color, val system: Color)

@Immutable
data class DayKeeperColorRoles(
  val calendar: CalendarColors,
  val priority: PriorityColors,
  val spaceType: SpaceTypeColors,
)

val LocalDayKeeperColorRoles =
  staticCompositionLocalOf<DayKeeperColorRoles> {
    error("No DayKeeperColorRoles provided. Wrap your composable in DayKeeperTheme.")
  }

val MaterialTheme.dayKeeperColors: DayKeeperColorRoles
  @Composable @ReadOnlyComposable get() = LocalDayKeeperColorRoles.current

private val calendarPalette =
  listOf(
    CalendarRed,
    CalendarOrange,
    CalendarAmber,
    CalendarYellow,
    CalendarLime,
    CalendarGreen,
    CalendarTeal,
    CalendarCyan,
    CalendarBlue,
    CalendarIndigo,
    CalendarPurple,
    CalendarPink,
  )

fun lightDayKeeperColorRoles(): DayKeeperColorRoles =
  DayKeeperColorRoles(
    calendar = CalendarColors(palette = calendarPalette),
    priority =
      PriorityColors(
        none = PriorityNoneLight,
        low = PriorityLowLight,
        medium = PriorityMediumLight,
        high = PriorityHighLight,
        urgent = PriorityUrgentLight,
      ),
    spaceType =
      SpaceTypeColors(
        personal = SpacePersonalLight,
        shared = SpaceSharedLight,
        system = SpaceSystemLight,
      ),
  )

fun darkDayKeeperColorRoles(): DayKeeperColorRoles =
  DayKeeperColorRoles(
    calendar = CalendarColors(palette = calendarPalette),
    priority =
      PriorityColors(
        none = PriorityNoneDark,
        low = PriorityLowDark,
        medium = PriorityMediumDark,
        high = PriorityHighDark,
        urgent = PriorityUrgentDark,
      ),
    spaceType =
      SpaceTypeColors(
        personal = SpacePersonalDark,
        shared = SpaceSharedDark,
        system = SpaceSystemDark,
      ),
  )
