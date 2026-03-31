package com.jsamuelsen11.daykeeper.core.data.attachment

import android.graphics.Bitmap
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ImageCompressorTest {

  private lateinit var compressor: ImageCompressorImpl

  @Before
  fun setup() {
    compressor = ImageCompressorImpl()
  }

  @Test
  fun `compress downsizes image exceeding maxDimension`() = runTest {
    val inputBytes = createTestImage(LARGE_WIDTH, LARGE_HEIGHT)

    val result = compressor.compress(inputBytes, "image/jpeg", maxDimension = SMALL_MAX_DIMENSION)

    result.width shouldBeLessThanOrEqual SMALL_MAX_DIMENSION
    result.height shouldBeLessThanOrEqual SMALL_MAX_DIMENSION
    result.mimeType shouldBe "image/jpeg"
    result.bytes.size shouldBeGreaterThan 0
  }

  @Test
  fun `compress preserves image within maxDimension`() = runTest {
    val inputBytes = createTestImage(SMALL_WIDTH, SMALL_HEIGHT)

    val result = compressor.compress(inputBytes, "image/jpeg", maxDimension = DEFAULT_MAX_DIMENSION)

    result.width shouldBe SMALL_WIDTH
    result.height shouldBe SMALL_HEIGHT
  }

  @Test
  fun `compress produces JPEG output`() = runTest {
    val inputBytes = createTestImage(SMALL_WIDTH, SMALL_HEIGHT)

    val result = compressor.compress(inputBytes, "image/png")

    result.mimeType shouldBe "image/jpeg"
  }

  @Test
  fun `compress maintains aspect ratio`() = runTest {
    val inputBytes = createTestImage(WIDE_WIDTH, WIDE_HEIGHT)

    val result = compressor.compress(inputBytes, "image/jpeg", maxDimension = SMALL_MAX_DIMENSION)

    val aspectRatio = result.width.toFloat() / result.height
    val expectedRatio = WIDE_WIDTH.toFloat() / WIDE_HEIGHT
    kotlin.math.abs(aspectRatio - expectedRatio) shouldBeLessThan ASPECT_RATIO_TOLERANCE
  }

  private fun createTestImage(width: Int, height: Int): ByteArray {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
    bitmap.recycle()
    return stream.toByteArray()
  }

  companion object {
    private const val LARGE_WIDTH = 4000
    private const val LARGE_HEIGHT = 3000
    private const val SMALL_WIDTH = 100
    private const val SMALL_HEIGHT = 80
    private const val WIDE_WIDTH = 2000
    private const val WIDE_HEIGHT = 1000
    private const val SMALL_MAX_DIMENSION = 500
    private const val DEFAULT_MAX_DIMENSION = 2048
    private const val JPEG_QUALITY = 90
    private const val ASPECT_RATIO_TOLERANCE = 0.1f
  }
}
