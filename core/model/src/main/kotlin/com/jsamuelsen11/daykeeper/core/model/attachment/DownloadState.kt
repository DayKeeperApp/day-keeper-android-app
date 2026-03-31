package com.jsamuelsen11.daykeeper.core.model.attachment

/** Represents the local download state of an attachment for UI rendering. */
public sealed interface DownloadState {
  public data object NotDownloaded : DownloadState

  public data class Downloading(val progress: Float) : DownloadState

  public data class Downloaded(val localPath: String) : DownloadState

  public data class Failed(val message: String) : DownloadState
}
