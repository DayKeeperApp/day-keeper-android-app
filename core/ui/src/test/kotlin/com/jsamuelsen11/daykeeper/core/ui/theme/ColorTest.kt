package com.jsamuelsen11.daykeeper.core.ui.theme

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ColorTest {

  private val calendarColors =
    listOf(
      CalendarRed,
      CalendarOrange,
      CalendarAmber,
      CalendarYellow,
      CalendarLime,
      CalendarGreen,
      CalendarTeal,
      CalendarCyan,
      CalendarBlue,
      CalendarIndigo,
      CalendarPurple,
      CalendarPink,
    )

  @Test
  fun `all calendar palette colors have full alpha`() {
    calendarColors.forEach { color -> color.alpha shouldBe 1.0f }
  }

  @Test
  fun `all priority light colors have full alpha`() {
    listOf(
        PriorityNoneLight,
        PriorityLowLight,
        PriorityMediumLight,
        PriorityHighLight,
        PriorityUrgentLight,
      )
      .forEach { color -> color.alpha shouldBe 1.0f }
  }

  @Test
  fun `all priority dark colors have full alpha`() {
    listOf(
        PriorityNoneDark,
        PriorityLowDark,
        PriorityMediumDark,
        PriorityHighDark,
        PriorityUrgentDark,
      )
      .forEach { color -> color.alpha shouldBe 1.0f }
  }

  @Test
  fun `all space type colors have full alpha`() {
    listOf(
        SpacePersonalLight,
        SpaceSharedLight,
        SpaceSystemLight,
        SpacePersonalDark,
        SpaceSharedDark,
        SpaceSystemDark,
      )
      .forEach { color -> color.alpha shouldBe 1.0f }
  }

  @Test
  fun `brand palette colors have full alpha`() {
    listOf(Teal40, Teal80, SlateBlue40, SlateBlue80, Amber40, Amber80).forEach { color ->
      color.alpha shouldBe 1.0f
    }
  }
}
