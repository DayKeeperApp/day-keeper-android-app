package com.jsamuelsen11.daykeeper.core.model.space

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class SpaceTest {

  private val space =
    Space(
      spaceId = "space-1",
      tenantId = "tenant-1",
      name = "My Space",
      normalizedName = "my space",
      type = SpaceType.PERSONAL,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    space.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `deletedAt defaults to null`() {
    space.deletedAt shouldBe null
  }
}
