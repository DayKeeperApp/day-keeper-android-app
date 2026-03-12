package com.jsamuelsen11.daykeeper.core.model.task

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TaskStatusTest {

  @Test
  fun `has four entries`() {
    TaskStatus.entries.size shouldBe 4
  }

  @Test
  fun `entries match schema values`() {
    TaskStatus.entries.map { it.name } shouldBe listOf("TODO", "IN_PROGRESS", "DONE", "CANCELLED")
  }
}
