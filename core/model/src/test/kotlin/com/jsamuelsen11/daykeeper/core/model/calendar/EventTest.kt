package com.jsamuelsen11.daykeeper.core.model.calendar

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class EventTest {

  private val timedEvent =
    Event(
      eventId = "event-1",
      calendarId = "cal-1",
      spaceId = "space-1",
      tenantId = "tenant-1",
      title = "Team standup",
      isAllDay = false,
      timezone = "America/New_York",
      startAt = 1_000_000L,
      endAt = 2_000_000L,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  private val allDayEvent =
    Event(
      eventId = "event-2",
      calendarId = "cal-1",
      spaceId = "space-1",
      tenantId = "tenant-1",
      title = "Holiday",
      isAllDay = true,
      timezone = "UTC",
      startDate = "2026-03-08",
      endDate = "2026-03-08",
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    timedEvent.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `optional fields default to null`() {
    timedEvent.description shouldBe null
    timedEvent.eventTypeId shouldBe null
    timedEvent.location shouldBe null
    timedEvent.recurrenceRule shouldBe null
    timedEvent.parentEventId shouldBe null
    timedEvent.deletedAt shouldBe null
  }

  @Test
  fun `all-day event uses date fields`() {
    allDayEvent.isAllDay shouldBe true
    allDayEvent.startDate shouldBe "2026-03-08"
    allDayEvent.startAt shouldBe null
  }
}
