package com.jsamuelsen11.daykeeper.core.ui.component

import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SwipeableListItemTest {

  @Test
  fun `swipe icon size is 24dp`() {
    SwipeIconSize shouldBe 24.dp
  }

  @Test
  fun `swipe background padding is 20dp`() {
    SwipeBackgroundPadding shouldBe 20.dp
  }
}
