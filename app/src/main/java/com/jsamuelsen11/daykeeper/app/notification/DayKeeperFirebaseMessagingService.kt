package com.jsamuelsen11.daykeeper.app.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jsamuelsen11.daykeeper.core.data.notification.NotificationDisplayManager
import org.koin.android.ext.android.inject

private const val KEY_TYPE = "type"
private const val KEY_TITLE = "title"
private const val KEY_BODY = "body"
private const val TYPE_SYNC = "sync"
private const val FCM_GENERAL_NOTIFICATION_ID = 5000

/** Handles incoming FCM messages and token refresh. */
class DayKeeperFirebaseMessagingService : FirebaseMessagingService() {

  private val displayManager: NotificationDisplayManager by inject()

  override fun onNewToken(token: String) {
    // Store token for future server registration.
    // Server registration will be implemented when the backend FCM integration is ready.
  }

  override fun onMessageReceived(message: RemoteMessage) {
    val data = message.data
    val type = data[KEY_TYPE]
    val title = data[KEY_TITLE] ?: message.notification?.title ?: return
    val body = data[KEY_BODY] ?: message.notification?.body ?: ""

    when (type) {
      TYPE_SYNC -> displayManager.showSyncNotification(body)
      else ->
        displayManager.showGeneralNotification(
          title,
          body,
          FCM_GENERAL_NOTIFICATION_ID + message.messageId.hashCode(),
        )
    }
  }
}
