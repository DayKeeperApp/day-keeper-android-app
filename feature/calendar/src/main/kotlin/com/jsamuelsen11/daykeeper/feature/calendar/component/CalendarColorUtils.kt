package com.jsamuelsen11.daykeeper.feature.calendar.component

import androidx.compose.ui.graphics.Color

private const val HEX_RADIX = 16
private const val HEX_RGB_LENGTH = 6
private const val HEX_ARGB_LENGTH = 8

/** Parses a hex color string (e.g. `"#4285F4"` or `"#FF4285F4"`) into a Compose [Color]. */
fun parseHexColor(hex: String): Color? =
  runCatching {
      val cleaned = hex.trimStart('#')
      val argb =
        when (cleaned.length) {
          HEX_RGB_LENGTH -> "FF$cleaned".toLong(radix = HEX_RADIX)
          HEX_ARGB_LENGTH -> cleaned.toLong(radix = HEX_RADIX)
          else -> return@runCatching null
        }
      Color(argb.toInt())
    }
    .getOrNull()
