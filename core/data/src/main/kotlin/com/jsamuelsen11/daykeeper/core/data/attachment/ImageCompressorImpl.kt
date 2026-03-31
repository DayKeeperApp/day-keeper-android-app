package com.jsamuelsen11.daykeeper.core.data.attachment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Default [ImageCompressor] implementation using Android [BitmapFactory].
 *
 * Decodes the input with optimal sample size for memory efficiency, scales if either dimension
 * exceeds the configured maximum, and compresses to JPEG.
 */
public class ImageCompressorImpl : ImageCompressor {

  override suspend fun compress(
    inputBytes: ByteArray,
    inputMimeType: String,
    maxDimension: Int,
    quality: Int,
  ): CompressedImage =
    withContext(Dispatchers.Default) {
      val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size, options)

      val originalWidth = options.outWidth
      val originalHeight = options.outHeight
      options.inSampleSize = calculateSampleSize(originalWidth, originalHeight, maxDimension)
      options.inJustDecodeBounds = false

      val sampled =
        BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size, options)
          ?: error("Failed to decode image")

      val scaled = scaleIfNeeded(sampled, maxDimension)
      val output = ByteArrayOutputStream()
      scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)

      val result =
        CompressedImage(
          bytes = output.toByteArray(),
          mimeType = "image/jpeg",
          width = scaled.width,
          height = scaled.height,
        )

      if (scaled !== sampled) scaled.recycle()
      sampled.recycle()
      result
    }
}

private fun calculateSampleSize(width: Int, height: Int, maxDimension: Int): Int {
  var sampleSize = 1
  val longerSide = max(width, height)
  if (longerSide > maxDimension) {
    val halfLonger = longerSide / 2
    while (halfLonger / sampleSize >= maxDimension) {
      sampleSize *= 2
    }
  }
  return sampleSize
}

private fun scaleIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
  val longerSide = max(bitmap.width, bitmap.height)
  if (longerSide <= maxDimension) return bitmap

  val ratio = maxDimension.toFloat() / longerSide
  val newWidth = (bitmap.width * ratio).roundToInt()
  val newHeight = (bitmap.height * ratio).roundToInt()
  return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}
