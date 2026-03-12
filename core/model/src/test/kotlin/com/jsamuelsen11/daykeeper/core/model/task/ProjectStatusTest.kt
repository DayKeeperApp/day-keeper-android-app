package com.jsamuelsen11.daykeeper.core.model.task

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ProjectStatusTest {

  @Test
  fun `has two entries`() {
    ProjectStatus.entries.size shouldBe 2
  }

  @Test
  fun `entries match schema values`() {
    ProjectStatus.entries.map { it.name } shouldBe listOf("ACTIVE", "ARCHIVED")
  }
}
