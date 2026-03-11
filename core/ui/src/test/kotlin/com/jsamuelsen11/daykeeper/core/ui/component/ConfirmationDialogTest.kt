package com.jsamuelsen11.daykeeper.core.ui.component

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ConfirmationDialogTest {

  @Test
  fun `default confirm label is Confirm`() {
    ConfirmationDialogDefaults.CONFIRM_LABEL shouldBe "Confirm"
  }

  @Test
  fun `default dismiss label is Cancel`() {
    ConfirmationDialogDefaults.DISMISS_LABEL shouldBe "Cancel"
  }
}
