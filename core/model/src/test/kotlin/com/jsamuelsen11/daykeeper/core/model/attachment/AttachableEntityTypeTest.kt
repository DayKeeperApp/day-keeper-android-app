package com.jsamuelsen11.daykeeper.core.model.attachment

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class AttachableEntityTypeTest {

  @Test
  fun `has three entries`() {
    AttachableEntityType.entries.size shouldBe 3
  }

  @Test
  fun `entries match schema values`() {
    AttachableEntityType.entries.map { it.name } shouldBe listOf("EVENT", "TASK", "PERSON")
  }
}
