package com.jsamuelsen11.daykeeper.core.model.space

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class SpaceMemberTest {

  private val member =
    SpaceMember(
      spaceId = "space-1",
      tenantId = "tenant-1",
      role = SpaceRole.EDITOR,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    member.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `deletedAt defaults to null`() {
    member.deletedAt shouldBe null
  }

  @Test
  fun `SpaceRole has three entries`() {
    SpaceRole.entries.size shouldBe 3
  }

  @Test
  fun `SpaceType has three entries`() {
    SpaceType.entries.size shouldBe 3
  }
}
