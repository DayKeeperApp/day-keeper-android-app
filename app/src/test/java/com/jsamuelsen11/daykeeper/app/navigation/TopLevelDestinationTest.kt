package com.jsamuelsen11.daykeeper.app.navigation

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TopLevelDestinationTest {

  @Test
  fun `entries contains exactly 5 destinations`() {
    TopLevelDestination.entries shouldHaveSize 5
  }

  @Test
  fun `destinations are in expected tab order`() {
    val expected =
      listOf(
        TopLevelDestination.CALENDAR,
        TopLevelDestination.TASKS,
        TopLevelDestination.LISTS,
        TopLevelDestination.PEOPLE,
        TopLevelDestination.PROFILE,
      )
    TopLevelDestination.entries shouldBe expected
  }

  @Test
  fun `each destination has a unique label resource id`() {
    val labelIds = TopLevelDestination.entries.map { it.labelResId }
    labelIds.distinct() shouldHaveSize labelIds.size
  }

  @Test
  fun `each destination has a unique route`() {
    val routes = TopLevelDestination.entries.map { it.route::class }
    routes.distinct() shouldHaveSize routes.size
  }
}
