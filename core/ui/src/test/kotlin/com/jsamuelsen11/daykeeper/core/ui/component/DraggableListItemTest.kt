package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.ui.unit.dp
import com.jsamuelsen11.daykeeper.core.ui.icon.DayKeeperIcons
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class DraggableListItemTest {

  @Test
  fun `drag handle size is 24dp`() {
    DragHandleSize shouldBe 24.dp
  }

  @Test
  fun `drag handle start padding is 4dp`() {
    DragHandleStartPadding shouldBe 4.dp
  }

  @Test
  fun `content start padding is 8dp`() {
    ContentStartPadding shouldBe 8.dp
  }

  @Test
  fun `item minimum height is 48dp`() {
    ItemMinHeight shouldBe 48.dp
  }

  @Test
  fun `DragHandle icon is a valid ImageVector`() {
    DayKeeperIcons.DragHandle shouldNotBe null
  }

  @Test
  fun `DragHandle icon has a non-blank name`() {
    DayKeeperIcons.DragHandle.name.shouldNotBeBlank()
  }
}
