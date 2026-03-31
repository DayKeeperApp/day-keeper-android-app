package com.jsamuelsen11.daykeeper.core.data.attachment

import com.jsamuelsen11.daykeeper.core.model.attachment.AttachableEntityType
import com.jsamuelsen11.daykeeper.core.model.attachment.Attachment
import com.jsamuelsen11.daykeeper.core.model.attachment.DownloadState
import java.io.File
import kotlinx.coroutines.flow.StateFlow

/** Orchestrates attachment file download, upload, and local cache management. */
public interface AttachmentManager {

  /** Observes the download state for the given [attachmentId]. */
  public fun observeDownloadState(attachmentId: String): StateFlow<DownloadState>

  /** Downloads the attachment content from the server and caches it locally. */
  public suspend fun download(attachment: Attachment)

  /**
   * Uploads a file as an attachment, optionally compressing images. Returns the created
   * [Attachment].
   */
  public suspend fun upload(
    entityType: AttachableEntityType,
    entityId: String,
    tenantId: String,
    spaceId: String,
    fileName: String,
    mimeType: String,
    fileBytes: ByteArray,
  ): Attachment

  /** Removes the local cached file for [attachmentId] without deleting the attachment record. */
  public suspend fun deleteLocal(attachmentId: String)

  /** Returns the cached file for [attachmentId], or `null` if not cached. */
  public fun getCachedFile(attachmentId: String): File?
}
