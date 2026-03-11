package com.jsamuelsen11.daykeeper.core.ui.component

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class DayKeeperDateTimePickerTest {

  @Test
  fun `default date title is Select date`() {
    DayKeeperDateTimePickerDefaults.DATE_TITLE shouldBe "Select date"
  }

  @Test
  fun `default time title is Select time`() {
    DayKeeperDateTimePickerDefaults.TIME_TITLE shouldBe "Select time"
  }

  @Test
  fun `default hour is 9`() {
    DayKeeperDateTimePickerDefaults.DEFAULT_HOUR shouldBe 9
  }

  @Test
  fun `default minute is 0`() {
    DayKeeperDateTimePickerDefaults.DEFAULT_MINUTE shouldBe 0
  }

  @Test
  fun `confirm label is not blank`() {
    DayKeeperDateTimePickerDefaults.CONFIRM_LABEL.shouldNotBeBlank()
  }

  @Test
  fun `dismiss label is not blank`() {
    DayKeeperDateTimePickerDefaults.DISMISS_LABEL.shouldNotBeBlank()
  }
}
