package com.jsamuelsen11.daykeeper.core.model.calendar

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class EventReminderTest {

  private val reminder =
    EventReminder(
      reminderId = "reminder-1",
      eventId = "event-1",
      minutesBefore = 15,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    reminder.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `deletedAt defaults to null`() {
    reminder.deletedAt shouldBe null
  }
}
