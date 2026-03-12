package com.jsamuelsen11.daykeeper.core.model.attachment

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class AttachmentTest {

  private val attachment =
    Attachment(
      attachmentId = "attach-1",
      entityType = AttachableEntityType.EVENT,
      entityId = "event-1",
      tenantId = "tenant-1",
      spaceId = "space-1",
      fileName = "photo.jpg",
      mimeType = "image/jpeg",
      fileSize = 1_024_000L,
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    attachment.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `optional fields default to null`() {
    attachment.remoteUrl shouldBe null
    attachment.localPath shouldBe null
    attachment.deletedAt shouldBe null
  }
}
