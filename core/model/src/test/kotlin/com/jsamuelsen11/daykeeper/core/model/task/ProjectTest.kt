package com.jsamuelsen11.daykeeper.core.model.task

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ProjectTest {

  private val project =
    Project(
      projectId = "proj-1",
      spaceId = "space-1",
      tenantId = "tenant-1",
      name = "Home Renovation",
      normalizedName = "home renovation",
      status = ProjectStatus.ACTIVE,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    project.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `optional fields default to null`() {
    project.description shouldBe null
    project.deletedAt shouldBe null
  }
}
