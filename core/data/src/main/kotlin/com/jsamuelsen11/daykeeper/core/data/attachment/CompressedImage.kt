package com.jsamuelsen11.daykeeper.core.data.attachment

/** Result of image compression containing the compressed bytes and metadata. */
public data class CompressedImage(
  val bytes: ByteArray,
  val mimeType: String,
  val width: Int,
  val height: Int,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CompressedImage) return false
    return bytes.contentEquals(other.bytes) &&
      mimeType == other.mimeType &&
      width == other.width &&
      height == other.height
  }

  override fun hashCode(): Int {
    var result = bytes.contentHashCode()
    result = 31 * result + mimeType.hashCode()
    result = 31 * result + width
    result = 31 * result + height
    return result
  }
}
