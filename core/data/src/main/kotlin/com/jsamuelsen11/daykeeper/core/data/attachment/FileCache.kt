package com.jsamuelsen11.daykeeper.core.data.attachment

import java.io.File

private const val HUNDRED_MB = 100L * 1024 * 1024

/**
 * LRU file cache for attachment content stored in app-internal storage.
 *
 * Files are keyed by attachment ID. Eviction is based on last-modified timestamp (oldest first) and
 * triggers when the total cache size exceeds [maxSizeBytes].
 */
public class FileCache(private val cacheDir: File, private val maxSizeBytes: Long = HUNDRED_MB) {

  init {
    cacheDir.mkdirs()
  }

  /** Returns the cached file for [attachmentId], or `null` if not cached. Touches on access. */
  @Synchronized
  public fun get(attachmentId: String): File? {
    val file = findFile(attachmentId) ?: return null
    file.setLastModified(System.currentTimeMillis())
    return file
  }

  /** Stores [data] in the cache under [attachmentId] with the given file [extension]. */
  @Synchronized
  public fun put(attachmentId: String, data: ByteArray, extension: String): File {
    findFile(attachmentId)?.delete()
    val file = File(cacheDir, "$attachmentId.$extension")
    file.writeBytes(data)
    evictIfNeeded()
    return file
  }

  /** Removes the cached file for [attachmentId]. */
  @Synchronized
  public fun remove(attachmentId: String) {
    findFile(attachmentId)?.delete()
  }

  /** Deletes all cached files. */
  @Synchronized
  public fun clear() {
    cacheDir.listFiles()?.forEach { it.delete() }
  }

  /** Returns the total size of all cached files in bytes. */
  @Synchronized
  public fun currentSizeBytes(): Long = cacheDir.listFiles()?.sumOf { it.length() } ?: 0L

  private fun findFile(attachmentId: String): File? =
    cacheDir.listFiles()?.firstOrNull { it.nameWithoutExtension == attachmentId }

  private fun evictIfNeeded() {
    val files = cacheDir.listFiles()?.toMutableList() ?: return
    var totalSize = files.sumOf { it.length() }
    if (totalSize <= maxSizeBytes) return

    files.sortBy { it.lastModified() }
    for (file in files) {
      if (totalSize <= maxSizeBytes) break
      totalSize -= file.length()
      file.delete()
    }
  }
}
