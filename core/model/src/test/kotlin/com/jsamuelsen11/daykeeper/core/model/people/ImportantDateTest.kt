package com.jsamuelsen11.daykeeper.core.model.people

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ImportantDateTest {

  private val importantDate =
    ImportantDate(
      importantDateId = "date-1",
      personId = "person-1",
      label = "Birthday",
      date = "1990-05-15",
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    importantDate.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `deletedAt defaults to null`() {
    importantDate.deletedAt shouldBe null
  }
}
