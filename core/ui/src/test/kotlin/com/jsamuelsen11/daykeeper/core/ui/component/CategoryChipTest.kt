package com.jsamuelsen11.daykeeper.core.ui.component

import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class CategoryChipTest {

  @Test
  fun `Label icon used for category chips is a valid ImageVector`() {
    DayKeeperIcons.Label shouldNotBe null
  }

  @Test
  fun `Label icon has a non-blank name`() {
    DayKeeperIcons.Label.name.shouldNotBeBlank()
  }

  @Test
  fun `Close icon used for dismiss is a valid ImageVector`() {
    DayKeeperIcons.Close shouldNotBe null
  }
}
