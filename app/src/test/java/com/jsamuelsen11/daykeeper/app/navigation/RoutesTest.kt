package com.jsamuelsen11.daykeeper.app.navigation

import com.jsamuelsen11.daykeeper.feature.lists.navigation.ListsHomeRoute
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class RoutesTest {

  @Test
  fun `CalendarRoute serializes and deserializes`() {
    val json = Json.encodeToString(CalendarRoute)
    val decoded = Json.decodeFromString<CalendarRoute>(json)
    decoded shouldBe CalendarRoute
  }

  @Test
  fun `all graph routes are distinct classes`() {
    val routes = listOf(CalendarRoute, TasksRoute, ListsRoute, PeopleRoute, ProfileRoute)
    val classes = routes.map { it::class }.distinct()
    classes.size shouldBe routes.size
  }

  @Test
  fun `all home routes are distinct classes`() {
    val routes =
      listOf(CalendarHomeRoute, TasksHomeRoute, ListsHomeRoute, PeopleHomeRoute, ProfileHomeRoute)
    val classes = routes.map { it::class }.distinct()
    classes.size shouldBe routes.size
  }

  @Test
  fun `GlobalSearchRoute is a serializable object`() {
    GlobalSearchRoute.shouldBeInstanceOf<GlobalSearchRoute>()
    val json = Json.encodeToString(GlobalSearchRoute)
    val decoded = Json.decodeFromString<GlobalSearchRoute>(json)
    decoded shouldBe GlobalSearchRoute
  }
}
