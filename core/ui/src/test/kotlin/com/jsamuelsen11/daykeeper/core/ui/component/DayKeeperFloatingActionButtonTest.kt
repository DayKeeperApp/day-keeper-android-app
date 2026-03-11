package com.jsamuelsen11.daykeeper.core.ui.component

import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class DayKeeperFloatingActionButtonTest {

  @Test
  fun `Add icon used for FAB is a valid ImageVector`() {
    DayKeeperIcons.Add shouldNotBe null
  }

  @Test
  fun `Add icon has a non-blank name`() {
    DayKeeperIcons.Add.name.shouldNotBeBlank()
  }
}
