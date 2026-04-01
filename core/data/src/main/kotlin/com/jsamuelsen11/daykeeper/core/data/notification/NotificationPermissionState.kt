package com.jsamuelsen11.daykeeper.core.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** State of the POST_NOTIFICATIONS runtime permission. */
public enum class NotificationPermissionState {
  GRANTED,
  DENIED,

  /** Android 12 or below — permission not required. */
  NOT_REQUIRED,
}

/** Checks the current POST_NOTIFICATIONS permission state. */
public fun Context.checkNotificationPermission(): NotificationPermissionState =
  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
    NotificationPermissionState.NOT_REQUIRED
  } else if (
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
      PackageManager.PERMISSION_GRANTED
  ) {
    NotificationPermissionState.GRANTED
  } else {
    NotificationPermissionState.DENIED
  }
