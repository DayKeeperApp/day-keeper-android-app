package com.jsamuelsen11.daykeeper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jsamuelsen11.daykeeper.app.ui.DayKeeperApp
import com.jsamuelsen11.daykeeper.core.ui.theme.DayKeeperTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { DayKeeperTheme { DayKeeperApp() } }
  }
}
