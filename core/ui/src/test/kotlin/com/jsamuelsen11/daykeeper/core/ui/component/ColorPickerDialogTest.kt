package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.ui.unit.dp
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class ColorPickerDialogTest {

  @Test
  fun `default title is Choose color`() {
    ColorPickerDialogDefaults.TITLE shouldBe "Choose color"
  }

  @Test
  fun `default columns is 4`() {
    ColorPickerDialogDefaults.COLUMNS shouldBe 4
  }

  @Test
  fun `columns is positive`() {
    ColorPickerDialogDefaults.COLUMNS shouldBeGreaterThan 0
  }

  @Test
  fun `swatch size is 48dp`() {
    ColorPickerDialogDefaults.COLOR_SWATCH_SIZE shouldBe 48.dp
  }

  @Test
  fun `swatch spacing is positive`() {
    ColorPickerDialogDefaults.SWATCH_SPACING shouldBe 8.dp
  }

  @Test
  fun `dismiss label is not blank`() {
    ColorPickerDialogDefaults.DISMISS_LABEL.shouldNotBeBlank()
  }
}
