package com.jsamuelsen11.daykeeper.core.model.calendar

import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class ReminderPresetTest {

  @Test
  fun `AT_TIME is 0 minutes before`() {
    ReminderPreset.AT_TIME.minutesBefore shouldBe 0
  }

  @Test
  fun `MINUTES_5 is 5 minutes before`() {
    ReminderPreset.MINUTES_5.minutesBefore shouldBe 5
  }

  @Test
  fun `MINUTES_15 is 15 minutes before`() {
    ReminderPreset.MINUTES_15.minutesBefore shouldBe 15
  }

  @Test
  fun `MINUTES_30 is 30 minutes before`() {
    ReminderPreset.MINUTES_30.minutesBefore shouldBe 30
  }

  @Test
  fun `HOURS_1 is 60 minutes before`() {
    ReminderPreset.HOURS_1.minutesBefore shouldBe 60
  }

  @Test
  fun `HOURS_2 is 120 minutes before`() {
    ReminderPreset.HOURS_2.minutesBefore shouldBe 120
  }

  @Test
  fun `DAYS_1 is 1440 minutes before`() {
    ReminderPreset.DAYS_1.minutesBefore shouldBe 1440
  }

  @Test
  fun `DAYS_2 is 2880 minutes before`() {
    ReminderPreset.DAYS_2.minutesBefore shouldBe 2880
  }

  @Test
  fun `WEEKS_1 is 10080 minutes before`() {
    ReminderPreset.WEEKS_1.minutesBefore shouldBe 10080
  }

  @Test
  fun `all presets have non-blank display labels`() {
    ReminderPreset.entries.forEach { preset -> preset.displayLabel.shouldNotBeBlank() }
  }

  @Test
  fun `all presets have unique minutes before values`() {
    val values = ReminderPreset.entries.map { it.minutesBefore }
    values.distinct().size shouldBe ReminderPreset.entries.size
  }

  @Test
  fun `all presets have non-negative minutes before`() {
    ReminderPreset.entries.forEach { preset -> preset.minutesBefore shouldBeGreaterThanOrEqual 0 }
  }

  @Test
  fun `presets are ordered by ascending minutes before`() {
    val values = ReminderPreset.entries.map { it.minutesBefore }
    values shouldBe values.sorted()
  }
}
