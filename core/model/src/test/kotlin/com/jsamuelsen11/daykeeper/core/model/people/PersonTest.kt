package com.jsamuelsen11.daykeeper.core.model.people

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class PersonTest {

  private val person =
    Person(
      personId = "person-1",
      spaceId = "space-1",
      tenantId = "tenant-1",
      firstName = "Jane",
      lastName = "Doe",
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    person.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `optional fields default to null`() {
    person.nickname shouldBe null
    person.notes shouldBe null
    person.deletedAt shouldBe null
  }
}
