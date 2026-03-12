package com.jsamuelsen11.daykeeper.core.ui.component

import com.jsamuelsen11.daykeeper.core.model.task.Priority
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PriorityIndicatorTest {

  @Test
  fun `priority enum has five values`() {
    Priority.entries.size shouldBe 5
  }

  @Test
  fun `priority entries are in severity order`() {
    Priority.entries.map { it.name } shouldBe listOf("NONE", "LOW", "MEDIUM", "HIGH", "URGENT")
  }
}
