package com.jsamuelsen11.daykeeper.core.model.attachment

/** UI-ready representation of an attachment combining metadata with download state. */
public data class AttachmentUiItem(
  val attachmentId: String,
  val fileName: String,
  val mimeType: String,
  val fileSize: Long,
  val downloadState: DownloadState,
  val remoteUrl: String? = null,
  val localPath: String? = null,
)
