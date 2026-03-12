package com.jsamuelsen11.daykeeper.core.model.account

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class WeekStartTest {

  @Test
  fun `has three entries`() {
    WeekStart.entries.size shouldBe 3
  }

  @Test
  fun `entries match schema values`() {
    WeekStart.entries.map { it.name } shouldBe listOf("SUNDAY", "MONDAY", "SATURDAY")
  }
}
