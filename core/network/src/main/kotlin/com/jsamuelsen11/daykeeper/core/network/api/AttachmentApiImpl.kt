package com.jsamuelsen11.daykeeper.core.network.api

import com.jsamuelsen11.daykeeper.core.network.dto.attachment.AttachmentUploadResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

private const val ATTACHMENT_UPLOAD_PATH = "api/v1/Attachments/upload"

class AttachmentApiImpl(private val httpClient: HttpClient) : AttachmentApi {

  override suspend fun upload(
    tenantId: String,
    spaceId: String,
    entityType: String,
    entityId: String,
    fileName: String,
    mimeType: String,
    fileBytes: ByteArray,
  ): AttachmentUploadResponseDto =
    httpClient
      .submitFormWithBinaryData(
        url = ATTACHMENT_UPLOAD_PATH,
        formData =
          formData {
            append("tenantId", tenantId)
            append("spaceId", spaceId)
            append("entityType", entityType)
            append("entityId", entityId)
            append(
              "file",
              fileBytes,
              Headers.build {
                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                append(HttpHeaders.ContentType, mimeType)
              },
            )
          },
      )
      .body()

  override suspend fun download(remoteUrl: String): ByteArray = httpClient.get(remoteUrl).body()
}
