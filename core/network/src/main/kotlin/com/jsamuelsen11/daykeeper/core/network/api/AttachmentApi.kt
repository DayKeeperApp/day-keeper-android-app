package com.jsamuelsen11.daykeeper.core.network.api

import com.jsamuelsen11.daykeeper.core.network.dto.attachment.AttachmentUploadResponseDto

/** Remote API for attachment file upload and download. */
interface AttachmentApi {

  suspend fun upload(
    tenantId: String,
    spaceId: String,
    entityType: String,
    entityId: String,
    fileName: String,
    mimeType: String,
    fileBytes: ByteArray,
  ): AttachmentUploadResponseDto

  suspend fun download(remoteUrl: String): ByteArray
}
