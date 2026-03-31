package com.jsamuelsen11.daykeeper.core.data.attachment

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.AttachmentRepository
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachableEntityType
import com.jsamuelsen11.daykeeper.core.model.attachment.Attachment
import com.jsamuelsen11.daykeeper.core.model.attachment.DownloadState
import com.jsamuelsen11.daykeeper.core.network.api.AttachmentApi
import com.jsamuelsen11.daykeeper.core.network.dto.attachment.AttachmentUploadResponseDto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class AttachmentManagerTest {

  @TempDir lateinit var tempDir: File

  private lateinit var attachmentRepository: AttachmentRepository
  private lateinit var attachmentApi: AttachmentApi
  private lateinit var fileCache: FileCache
  private lateinit var manager: AttachmentManagerImpl

  @BeforeEach
  fun setup() {
    attachmentRepository = mockk(relaxed = true)
    attachmentApi = mockk()
    fileCache = FileCache(File(tempDir, "cache"))
    manager = AttachmentManagerImpl(attachmentRepository, attachmentApi, fileCache)
  }

  @Test
  fun `download fetches from API when not cached`() = runTest {
    val bytes = ByteArray(DOWNLOAD_SIZE) { it.toByte() }
    coEvery { attachmentApi.download(TEST_REMOTE_URL) } returns bytes

    manager.download(testAttachment())

    coVerify { attachmentApi.download(TEST_REMOTE_URL) }
    fileCache.get(TEST_ATTACHMENT_ID)?.readBytes() shouldBe bytes
  }

  @Test
  fun `download emits Downloaded state on success`() = runTest {
    val bytes = ByteArray(DOWNLOAD_SIZE) { it.toByte() }
    coEvery { attachmentApi.download(TEST_REMOTE_URL) } returns bytes

    manager.download(testAttachment())

    manager.observeDownloadState(TEST_ATTACHMENT_ID).test {
      awaitItem().shouldBeInstanceOf<DownloadState.Downloaded>()
    }
  }

  @Test
  fun `download emits Failed state on API error`() = runTest {
    coEvery { attachmentApi.download(TEST_REMOTE_URL) } throws java.io.IOException("Network error")

    manager.download(testAttachment())

    manager.observeDownloadState(TEST_ATTACHMENT_ID).test {
      val state = awaitItem()
      state.shouldBeInstanceOf<DownloadState.Failed>()
      state.message shouldBe "Network error"
    }
  }

  @Test
  fun `download emits Failed when no remote URL`() = runTest {
    manager.download(testAttachment(remoteUrl = null))

    manager.observeDownloadState(TEST_ATTACHMENT_ID).test {
      val state = awaitItem()
      state.shouldBeInstanceOf<DownloadState.Failed>()
      state.message shouldBe "No remote URL"
    }
  }

  @Test
  fun `upload calls API and creates attachment record`() = runTest {
    val bytes = ByteArray(UPLOAD_SIZE) { it.toByte() }
    coEvery {
      attachmentApi.upload(
        tenantId = TEST_TENANT_ID,
        spaceId = TEST_SPACE_ID,
        entityType = "TASK",
        entityId = TEST_ENTITY_ID,
        fileName = "photo.jpg",
        mimeType = "image/jpeg",
        fileBytes = bytes,
      )
    } returns
      AttachmentUploadResponseDto(remoteUrl = TEST_REMOTE_URL, fileSize = bytes.size.toLong())

    val result =
      manager.upload(
        entityType = AttachableEntityType.TASK,
        entityId = TEST_ENTITY_ID,
        tenantId = TEST_TENANT_ID,
        spaceId = TEST_SPACE_ID,
        fileName = "photo.jpg",
        mimeType = "image/jpeg",
        fileBytes = bytes,
      )

    result.fileName shouldBe "photo.jpg"
    result.mimeType shouldBe "image/jpeg"
    result.remoteUrl shouldBe TEST_REMOTE_URL
    coVerify { attachmentRepository.upsert(any()) }
  }

  @Test
  fun `deleteLocal removes from cache and clears localPath`() = runTest {
    fileCache.put(TEST_ATTACHMENT_ID, ByteArray(DOWNLOAD_SIZE), "jpg")
    coEvery { attachmentRepository.getById(TEST_ATTACHMENT_ID) } returns testAttachment()

    manager.deleteLocal(TEST_ATTACHMENT_ID)

    fileCache.get(TEST_ATTACHMENT_ID) shouldBe null
    coVerify {
      attachmentRepository.upsert(
        match { it.attachmentId == TEST_ATTACHMENT_ID && it.localPath == null }
      )
    }
  }

  @Test
  fun `getCachedFile returns null when not cached`() {
    manager.getCachedFile("missing") shouldBe null
  }

  @Test
  fun `getCachedFile returns file when cached`() {
    val data = ByteArray(DOWNLOAD_SIZE) { it.toByte() }
    fileCache.put(TEST_ATTACHMENT_ID, data, "jpg")
    val file = manager.getCachedFile(TEST_ATTACHMENT_ID)
    file?.readBytes() shouldBe data
  }

  @Test
  fun `observeDownloadState returns NotDownloaded for unknown attachment`() = runTest {
    manager.observeDownloadState("unknown").test {
      awaitItem().shouldBeInstanceOf<DownloadState.NotDownloaded>()
    }
  }

  private fun testAttachment(remoteUrl: String? = TEST_REMOTE_URL) =
    Attachment(
      attachmentId = TEST_ATTACHMENT_ID,
      entityType = AttachableEntityType.TASK,
      entityId = TEST_ENTITY_ID,
      tenantId = TEST_TENANT_ID,
      spaceId = TEST_SPACE_ID,
      fileName = "test.jpg",
      mimeType = "image/jpeg",
      fileSize = DOWNLOAD_SIZE.toLong(),
      remoteUrl = remoteUrl,
      localPath = null,
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis(),
    )

  companion object {
    private const val TEST_ATTACHMENT_ID = "att-123"
    private const val TEST_ENTITY_ID = "entity-456"
    private const val TEST_TENANT_ID = "tenant-789"
    private const val TEST_SPACE_ID = "space-abc"
    private const val TEST_REMOTE_URL = "https://example.com/files/test.jpg"
    private const val DOWNLOAD_SIZE = 256
    private const val UPLOAD_SIZE = 128
  }
}
