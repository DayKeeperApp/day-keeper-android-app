package com.jsamuelsen11.daykeeper.core.model.sync

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class SyncCursorTest {

  private val cursor = SyncCursor(lastCursor = 42L, lastSyncAt = 1_000_000L)

  @Test
  fun `implements DayKeeperModel`() {
    cursor.shouldBeInstanceOf<DayKeeperModel>()
  }
}
