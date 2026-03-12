package com.jsamuelsen11.daykeeper.core.model.account

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class AccountTest {

  private val account =
    Account(
      tenantId = "tenant-1",
      displayName = "Test User",
      email = "test@example.com",
      timezone = "America/New_York",
      weekStart = WeekStart.SUNDAY,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    account.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `deletedAt defaults to null`() {
    account.deletedAt shouldBe null
  }

  @Test
  fun `copy preserves all fields`() {
    val copied = account.copy(displayName = "Updated")
    copied.displayName shouldBe "Updated"
    copied.tenantId shouldBe account.tenantId
  }
}
