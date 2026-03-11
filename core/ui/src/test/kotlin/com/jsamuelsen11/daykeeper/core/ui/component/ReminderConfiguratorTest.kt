package com.jsamuelsen11.daykeeper.core.ui.component

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class ReminderConfiguratorTest {

  @Test
  fun `default title is Reminder`() {
    ReminderConfiguratorDefaults.TITLE shouldBe "Reminder"
  }

  @Test
  fun `custom label is not blank`() {
    ReminderConfiguratorDefaults.CUSTOM_LABEL.shouldNotBeBlank()
  }

  @Test
  fun `custom minutes label is not blank`() {
    ReminderConfiguratorDefaults.CUSTOM_MINUTES_LABEL.shouldNotBeBlank()
  }

  @Test
  fun `min custom minutes is 1`() {
    ReminderConfiguratorDefaults.MIN_CUSTOM_MINUTES shouldBe 1
  }

  @Test
  fun `max custom minutes is 40320`() {
    ReminderConfiguratorDefaults.MAX_CUSTOM_MINUTES shouldBe 40320
  }

  @Test
  fun `max is greater than min`() {
    ReminderConfiguratorDefaults.MAX_CUSTOM_MINUTES shouldBeGreaterThan
      ReminderConfiguratorDefaults.MIN_CUSTOM_MINUTES
  }
}
