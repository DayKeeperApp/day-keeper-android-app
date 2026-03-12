package com.jsamuelsen11.daykeeper.core.model.task

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TaskTest {

  private val task =
    Task(
      taskId = "task-1",
      spaceId = "space-1",
      tenantId = "tenant-1",
      title = "Buy groceries",
      status = TaskStatus.TODO,
      priority = Priority.MEDIUM,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    task.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `optional fields default to null`() {
    task.projectId shouldBe null
    task.description shouldBe null
    task.dueAt shouldBe null
    task.dueDate shouldBe null
    task.recurrenceRule shouldBe null
    task.categoryId shouldBe null
    task.deletedAt shouldBe null
  }

  @Test
  fun `Priority has five entries`() {
    Priority.entries.size shouldBe 5
  }
}
