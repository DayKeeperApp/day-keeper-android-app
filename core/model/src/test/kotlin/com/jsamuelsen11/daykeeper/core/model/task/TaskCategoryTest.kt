package com.jsamuelsen11.daykeeper.core.model.task

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TaskCategoryTest {

  private val category =
    TaskCategory(
      categoryId = "cat-1",
      name = "Errands",
      normalizedName = "errands",
      isSystem = false,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    category.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `color defaults to null`() {
    category.color shouldBe null
  }
}
