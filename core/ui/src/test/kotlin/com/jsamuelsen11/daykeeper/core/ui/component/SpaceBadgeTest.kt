package com.jsamuelsen11.daykeeper.core.ui.component

import com.jsamuelsen11.daykeeper.core.model.SpaceRole
import com.jsamuelsen11.daykeeper.core.model.SpaceType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SpaceBadgeTest {

  @Test
  fun `space type enum has three values`() {
    SpaceType.entries.size shouldBe 3
  }

  @Test
  fun `space type entries match design doc`() {
    SpaceType.entries.map { it.name } shouldBe listOf("PERSONAL", "SHARED", "SYSTEM")
  }

  @Test
  fun `space role enum has three values`() {
    SpaceRole.entries.size shouldBe 3
  }

  @Test
  fun `space role entries match design doc`() {
    SpaceRole.entries.map { it.name } shouldBe listOf("OWNER", "EDITOR", "VIEWER")
  }
}
