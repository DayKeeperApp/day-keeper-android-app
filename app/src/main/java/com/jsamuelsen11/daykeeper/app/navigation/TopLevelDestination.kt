package com.jsamuelsen11.daykeeper.app.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.jsamuelsen11.daykeeper.app.R
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons

enum class TopLevelDestination(
  @param:StringRes val labelResId: Int,
  val icon: ImageVector,
  val route: Any,
) {
  CALENDAR(
    labelResId = R.string.nav_calendar,
    icon = DayKeeperIcons.Calendar,
    route = CalendarRoute,
  ),
  TASKS(labelResId = R.string.nav_tasks, icon = DayKeeperIcons.Tasks, route = TasksRoute),
  LISTS(labelResId = R.string.nav_lists, icon = DayKeeperIcons.Lists, route = ListsRoute),
  PEOPLE(labelResId = R.string.nav_people, icon = DayKeeperIcons.People, route = PeopleRoute),
  PROFILE(labelResId = R.string.nav_profile, icon = DayKeeperIcons.Profile, route = ProfileRoute),
}
