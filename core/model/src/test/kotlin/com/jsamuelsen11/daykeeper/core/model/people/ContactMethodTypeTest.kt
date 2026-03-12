package com.jsamuelsen11.daykeeper.core.model.people

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ContactMethodTypeTest {

  @Test
  fun `has two entries`() {
    ContactMethodType.entries.size shouldBe 2
  }

  @Test
  fun `entries match schema values`() {
    ContactMethodType.entries.map { it.name } shouldBe listOf("PHONE", "EMAIL")
  }
}
