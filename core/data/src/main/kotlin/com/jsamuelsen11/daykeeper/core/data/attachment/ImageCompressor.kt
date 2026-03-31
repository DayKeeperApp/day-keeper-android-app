package com.jsamuelsen11.daykeeper.core.data.attachment

/** Compresses and resizes images before upload. */
public interface ImageCompressor {

  /**
   * Compresses the input image bytes. Images exceeding [maxDimension] in either axis are scaled
   * down proportionally. Output is JPEG at [quality] percent.
   */
  public suspend fun compress(
    inputBytes: ByteArray,
    inputMimeType: String,
    maxDimension: Int = DEFAULT_MAX_DIMENSION,
    quality: Int = DEFAULT_QUALITY,
  ): CompressedImage

  public companion object {
    public const val DEFAULT_MAX_DIMENSION: Int = 2048
    public const val DEFAULT_QUALITY: Int = 80
  }
}
