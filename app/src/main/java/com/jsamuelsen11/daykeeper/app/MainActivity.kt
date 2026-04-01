package com.jsamuelsen11.daykeeper.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jsamuelsen11.daykeeper.app.ui.DayKeeperApp
import com.jsamuelsen11.daykeeper.core.data.notification.DeepLinkConstants
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

class MainActivity : ComponentActivity() {

  private var deepLinkRoute by mutableStateOf<Any?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    deepLinkRoute = parseDeepLink(intent)
    setContent { DayKeeperTheme { DayKeeperApp(deepLinkRoute = deepLinkRoute) } }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    deepLinkRoute = parseDeepLink(intent)
  }

  private fun parseDeepLink(intent: Intent): Any? {
    val type = intent.getStringExtra(DeepLinkConstants.EXTRA_DEEP_LINK_TYPE) ?: return null
    val entityId = intent.getStringExtra(DeepLinkConstants.EXTRA_ENTITY_ID) ?: return null
    return DeepLinkRoute(type, entityId)
  }
}

data class DeepLinkRoute(val type: String, val entityId: String)
