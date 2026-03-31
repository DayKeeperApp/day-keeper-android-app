package com.jsamuelsen11.daykeeper.core.network.dto.attachment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttachmentUploadResponseDto(
  @SerialName("remoteUrl") val remoteUrl: String,
  @SerialName("fileSize") val fileSize: Long,
)
