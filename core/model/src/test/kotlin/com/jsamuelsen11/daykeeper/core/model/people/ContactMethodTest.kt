package com.jsamuelsen11.daykeeper.core.model.people

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ContactMethodTest {

  private val contact =
    ContactMethod(
      contactMethodId = "cm-1",
      personId = "person-1",
      type = ContactMethodType.EMAIL,
      value = "jane@example.com",
      label = "Work",
      isPrimary = true,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    contact.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `deletedAt defaults to null`() {
    contact.deletedAt shouldBe null
  }
}
