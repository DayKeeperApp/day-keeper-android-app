package com.jsamuelsen11.daykeeper.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ShapeTest {

  @Test
  fun `all shape sizes are RoundedCornerShape`() {
    DayKeeperShapes.extraSmall.shouldBeInstanceOf<RoundedCornerShape>()
    DayKeeperShapes.small.shouldBeInstanceOf<RoundedCornerShape>()
    DayKeeperShapes.medium.shouldBeInstanceOf<RoundedCornerShape>()
    DayKeeperShapes.large.shouldBeInstanceOf<RoundedCornerShape>()
    DayKeeperShapes.extraLarge.shouldBeInstanceOf<RoundedCornerShape>()
  }
}
