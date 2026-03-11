package com.jsamuelsen11.daykeeper.core.ui.component

import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class DayKeeperSearchBarTest {

  @Test
  fun `Search icon is a valid ImageVector`() {
    DayKeeperIcons.Search shouldNotBe null
  }

  @Test
  fun `Search icon has a non-blank name`() {
    DayKeeperIcons.Search.name.shouldNotBeBlank()
  }

  @Test
  fun `Close icon for clear button is a valid ImageVector`() {
    DayKeeperIcons.Close shouldNotBe null
  }
}
