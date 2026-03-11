package com.jsamuelsen11.daykeeper.core.ui.component

import com.jsamuelsen11.daykeeper.core.model.RecurrenceDay
import com.jsamuelsen11.daykeeper.core.model.RecurrenceFrequency
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class RecurrencePickerTest {

  @Test
  fun `default title is Repeat`() {
    RecurrencePickerDefaults.TITLE shouldBe "Repeat"
  }

  @Test
  fun `default interval is 1`() {
    RecurrencePickerDefaults.DEFAULT_INTERVAL shouldBe 1
  }

  @Test
  fun `min interval is 1`() {
    RecurrencePickerDefaults.MIN_INTERVAL shouldBe 1
  }

  @Test
  fun `max interval is 99`() {
    RecurrencePickerDefaults.MAX_INTERVAL shouldBe 99
  }

  @Test
  fun `max interval is greater than min`() {
    RecurrencePickerDefaults.MAX_INTERVAL shouldBeGreaterThan RecurrencePickerDefaults.MIN_INTERVAL
  }

  @Test
  fun `default occurrences is 10`() {
    RecurrencePickerDefaults.DEFAULT_OCCURRENCES shouldBe 10
  }

  @Test
  fun `frequency labels cover all frequencies`() {
    RecurrencePickerDefaults.frequencyLabels shouldHaveSize RecurrenceFrequency.entries.size
  }

  @Test
  fun `frequency units cover all frequencies`() {
    RecurrencePickerDefaults.frequencyUnits shouldHaveSize RecurrenceFrequency.entries.size
  }

  @Test
  fun `day labels cover all days`() {
    RecurrencePickerDefaults.dayLabels shouldHaveSize RecurrenceDay.entries.size
  }

  @Test
  fun `all labels are non-blank`() {
    RecurrencePickerDefaults.TITLE.shouldNotBeBlank()
    RecurrencePickerDefaults.INTERVAL_LABEL.shouldNotBeBlank()
    RecurrencePickerDefaults.ENDS_LABEL.shouldNotBeBlank()
    RecurrencePickerDefaults.NEVER_LABEL.shouldNotBeBlank()
    RecurrencePickerDefaults.AFTER_LABEL.shouldNotBeBlank()
    RecurrencePickerDefaults.ON_DATE_LABEL.shouldNotBeBlank()
    RecurrencePickerDefaults.OCCURRENCES_LABEL.shouldNotBeBlank()
    RecurrencePickerDefaults.SAVE_LABEL.shouldNotBeBlank()
    RecurrencePickerDefaults.NO_REPEAT_LABEL.shouldNotBeBlank()
  }
}
