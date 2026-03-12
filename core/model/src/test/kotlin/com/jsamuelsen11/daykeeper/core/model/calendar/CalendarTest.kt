package com.jsamuelsen11.daykeeper.core.model.calendar

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class CalendarTest {

  private val calendar =
    Calendar(
      calendarId = "cal-1",
      spaceId = "space-1",
      tenantId = "tenant-1",
      name = "Work",
      normalizedName = "work",
      color = "#FF5733",
      isDefault = true,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    calendar.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `deletedAt defaults to null`() {
    calendar.deletedAt shouldBe null
  }
}
