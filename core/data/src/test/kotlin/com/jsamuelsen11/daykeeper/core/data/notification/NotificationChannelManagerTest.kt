package com.jsamuelsen11.daykeeper.core.data.notification

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NotificationChannelManagerTest {

  private lateinit var context: Context
  private lateinit var channelManager: NotificationChannelManager
  private lateinit var notificationManager: NotificationManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    channelManager = NotificationChannelManager(context)
    notificationManager = context.getSystemService(NotificationManager::class.java)
  }

  @Test
  fun `createChannels creates reminders channel with high importance`() {
    channelManager.createChannels()

    val channel = notificationManager.getNotificationChannel(CHANNEL_REMINDERS)
    channel.shouldNotBeNull()
    channel.importance shouldBe NotificationManager.IMPORTANCE_HIGH
  }

  @Test
  fun `createChannels creates sync channel with low importance`() {
    channelManager.createChannels()

    val channel = notificationManager.getNotificationChannel(CHANNEL_SYNC)
    channel.shouldNotBeNull()
    channel.importance shouldBe NotificationManager.IMPORTANCE_LOW
  }

  @Test
  fun `createChannels creates general channel with default importance`() {
    channelManager.createChannels()

    val channel = notificationManager.getNotificationChannel(CHANNEL_GENERAL)
    channel.shouldNotBeNull()
    channel.importance shouldBe NotificationManager.IMPORTANCE_DEFAULT
  }

  @Test
  fun `createChannels is idempotent`() {
    channelManager.createChannels()
    channelManager.createChannels()

    notificationManager.notificationChannels.size shouldBe EXPECTED_CHANNEL_COUNT
  }

  companion object {
    private const val CHANNEL_REMINDERS = NotificationChannelManager.CHANNEL_REMINDERS
    private const val CHANNEL_SYNC = NotificationChannelManager.CHANNEL_SYNC
    private const val CHANNEL_GENERAL = NotificationChannelManager.CHANNEL_GENERAL
    private const val EXPECTED_CHANNEL_COUNT = 3
  }
}
