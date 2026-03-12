package com.jsamuelsen11.daykeeper.core.model.account

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class DeviceTest {

  private val device =
    Device(
      deviceId = "device-1",
      tenantId = "tenant-1",
      deviceName = "Pixel 9",
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    device.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `fcmToken defaults to null`() {
    device.fcmToken shouldBe null
  }

  @Test
  fun `lastSyncCursor defaults to zero`() {
    device.lastSyncCursor shouldBe 0L
  }
}
