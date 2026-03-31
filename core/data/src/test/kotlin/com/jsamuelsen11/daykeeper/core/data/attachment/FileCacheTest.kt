package com.jsamuelsen11.daykeeper.core.data.attachment

import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class FileCacheTest {

  @TempDir lateinit var tempDir: File

  private lateinit var cache: FileCache

  private val smallData = ByteArray(SMALL_FILE_SIZE) { it.toByte() }
  private val largeData = ByteArray(LARGE_FILE_SIZE) { it.toByte() }

  @BeforeEach
  fun setup() {
    val cacheDir = File(tempDir, "cache")
    cache = FileCache(cacheDir, maxSizeBytes = MAX_CACHE_SIZE)
  }

  @Test
  fun `put stores file and get retrieves it`() {
    val file = cache.put("att-1", smallData, "jpg")
    file.exists() shouldBe true
    file.readBytes() shouldBe smallData

    val retrieved = cache.get("att-1")
    retrieved shouldBe file
  }

  @Test
  fun `get returns null for missing attachmentId`() {
    cache.get("nonexistent").shouldBeNull()
  }

  @Test
  fun `remove deletes specific file`() {
    cache.put("att-1", smallData, "jpg")
    cache.remove("att-1")
    cache.get("att-1").shouldBeNull()
  }

  @Test
  fun `clear removes all cached files`() {
    cache.put("att-1", smallData, "jpg")
    cache.put("att-2", smallData, "png")
    cache.clear()
    cache.get("att-1").shouldBeNull()
    cache.get("att-2").shouldBeNull()
    cache.currentSizeBytes() shouldBe 0L
  }

  @Test
  fun `currentSizeBytes reflects total file size`() {
    cache.put("att-1", smallData, "jpg")
    cache.put("att-2", smallData, "png")
    cache.currentSizeBytes() shouldBe (SMALL_FILE_SIZE * 2).toLong()
  }

  @Test
  fun `eviction removes oldest file when over size cap`() {
    cache.put("att-old", largeData, "jpg")
    Thread.sleep(EVICTION_DELAY_MS)
    cache.put("att-new", largeData, "png")

    cache.get("att-old").shouldBeNull()
    cache.get("att-new")?.exists() shouldBe true
    cache.currentSizeBytes() shouldBe LARGE_FILE_SIZE.toLong()
  }

  @Test
  fun `put overwrites existing file with same id`() {
    cache.put("att-1", smallData, "jpg")
    val newData = ByteArray(NEW_DATA_SIZE) { 0xFF.toByte() }
    cache.put("att-1", newData, "png")

    val file = cache.get("att-1")
    file?.readBytes() shouldBe newData
    file?.name shouldBe "att-1.png"
  }

  @Test
  fun `get touches file to mark recent access`() {
    cache.put("att-1", smallData, "jpg")
    val initialModified = cache.get("att-1")?.lastModified() ?: 0L
    Thread.sleep(EVICTION_DELAY_MS)
    cache.get("att-1")
    val updatedModified = cache.get("att-1")?.lastModified() ?: 0L
    updatedModified shouldBeGreaterThan initialModified
  }

  companion object {
    private const val SMALL_FILE_SIZE = 100
    private const val LARGE_FILE_SIZE = 600
    private const val NEW_DATA_SIZE = 50
    private const val MAX_CACHE_SIZE = 1000L
    private const val EVICTION_DELAY_MS = 50L
  }
}
