package com.jsamuelsen11.daykeeper.core.model.calendar

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class EventTypeTest {

  private val eventType =
    EventType(
      eventTypeId = "type-1",
      name = "Meeting",
      normalizedName = "meeting",
      isSystem = true,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    eventType.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `color defaults to null`() {
    eventType.color shouldBe null
  }
}
