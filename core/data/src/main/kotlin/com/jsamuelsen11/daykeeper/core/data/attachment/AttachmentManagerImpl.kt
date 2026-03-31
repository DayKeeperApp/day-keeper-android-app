package com.jsamuelsen11.daykeeper.core.data.attachment

import com.jsamuelsen11.daykeeper.core.data.repository.AttachmentRepository
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachableEntityType
import com.jsamuelsen11.daykeeper.core.model.attachment.Attachment
import com.jsamuelsen11.daykeeper.core.model.attachment.DownloadState
import com.jsamuelsen11.daykeeper.core.network.api.AttachmentApi
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

private const val INITIAL_PROGRESS = 0f
private const val COMPLETE_PROGRESS = 1f

/**
 * Default implementation of [AttachmentManager].
 *
 * Coordinates between [AttachmentApi] for remote operations, [FileCache] for local storage, and
 * [AttachmentRepository] for metadata persistence. Tracks per-attachment download state for UI
 * consumption.
 */
public class AttachmentManagerImpl(
  private val attachmentRepository: AttachmentRepository,
  private val attachmentApi: AttachmentApi,
  private val fileCache: FileCache,
) : AttachmentManager {

  private val downloadStates = ConcurrentHashMap<String, MutableStateFlow<DownloadState>>()

  override fun observeDownloadState(attachmentId: String): StateFlow<DownloadState> =
    downloadStates.getOrPut(attachmentId) {
      val cached = fileCache.get(attachmentId)
      MutableStateFlow(
        if (cached != null) DownloadState.Downloaded(cached.absolutePath)
        else DownloadState.NotDownloaded
      )
    }

  override suspend fun download(attachment: Attachment) {
    val state = stateFlow(attachment.attachmentId)
    val remoteUrl = attachment.remoteUrl
    if (remoteUrl == null) {
      state.value = DownloadState.Failed("No remote URL")
      return
    }

    state.value = DownloadState.Downloading(INITIAL_PROGRESS)
    try {
      val bytes = withContext(Dispatchers.IO) { attachmentApi.download(remoteUrl) }

      state.value = DownloadState.Downloading(COMPLETE_PROGRESS)

      val extension = extensionFromMimeType(attachment.mimeType)
      val file =
        withContext(Dispatchers.IO) { fileCache.put(attachment.attachmentId, bytes, extension) }

      attachmentRepository.upsert(attachment.copy(localPath = file.absolutePath))
      state.value = DownloadState.Downloaded(file.absolutePath)
    } catch (e: java.io.IOException) {
      state.value = DownloadState.Failed(e.message ?: "Download failed")
    }
  }

  override suspend fun upload(
    entityType: AttachableEntityType,
    entityId: String,
    tenantId: String,
    spaceId: String,
    fileName: String,
    mimeType: String,
    fileBytes: ByteArray,
  ): Attachment {
    val response =
      attachmentApi.upload(
        tenantId = tenantId,
        spaceId = spaceId,
        entityType = entityType.name,
        entityId = entityId,
        fileName = fileName,
        mimeType = mimeType,
        fileBytes = fileBytes,
      )

    val attachmentId = UUID.randomUUID().toString()
    val extension = extensionFromMimeType(mimeType)
    val localFile =
      withContext(Dispatchers.IO) { fileCache.put(attachmentId, fileBytes, extension) }

    val now = System.currentTimeMillis()
    val attachment =
      Attachment(
        attachmentId = attachmentId,
        entityType = entityType,
        entityId = entityId,
        tenantId = tenantId,
        spaceId = spaceId,
        fileName = fileName,
        mimeType = mimeType,
        fileSize = response.fileSize,
        remoteUrl = response.remoteUrl,
        localPath = localFile.absolutePath,
        createdAt = now,
        updatedAt = now,
      )

    attachmentRepository.upsert(attachment)
    stateFlow(attachmentId).value = DownloadState.Downloaded(localFile.absolutePath)
    return attachment
  }

  override suspend fun deleteLocal(attachmentId: String) {
    withContext(Dispatchers.IO) { fileCache.remove(attachmentId) }
    val attachment = attachmentRepository.getById(attachmentId)
    if (attachment != null) {
      attachmentRepository.upsert(attachment.copy(localPath = null))
    }
    stateFlow(attachmentId).value = DownloadState.NotDownloaded
  }

  override fun getCachedFile(attachmentId: String): File? = fileCache.get(attachmentId)

  private fun stateFlow(attachmentId: String): MutableStateFlow<DownloadState> =
    downloadStates.getOrPut(attachmentId) { MutableStateFlow(DownloadState.NotDownloaded) }
}

private fun extensionFromMimeType(mimeType: String): String =
  when (mimeType) {
    "image/jpeg" -> "jpg"
    "image/png" -> "png"
    "image/webp" -> "webp"
    "image/heic" -> "heic"
    "application/pdf" -> "pdf"
    else -> "bin"
  }
