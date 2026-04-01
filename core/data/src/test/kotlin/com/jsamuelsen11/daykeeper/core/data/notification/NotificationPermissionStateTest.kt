package com.jsamuelsen11.daykeeper.core.data.notification

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class NotificationPermissionStateTest {

  private val context: Context = ApplicationProvider.getApplicationContext()

  @Test
  @Config(sdk = [Build.VERSION_CODES.S])
  fun `returns NOT_REQUIRED on Android 12`() {
    context.checkNotificationPermission() shouldBe NotificationPermissionState.NOT_REQUIRED
  }

  @Test
  @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
  fun `returns DENIED on Android 13 without permission`() {
    context.checkNotificationPermission() shouldBe NotificationPermissionState.DENIED
  }
}
