package com.jsamuelsen11.daykeeper.core.ui.theme

import androidx.compose.ui.graphics.Color
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

private const val EXPECTED_CALENDAR_PALETTE_SIZE = 12

class DayKeeperColorRolesTest {

  @Test
  fun `light color roles returns valid non-unspecified colors`() {
    val roles = lightDayKeeperColorRoles()

    roles.priority.none shouldNotBe Color.Unspecified
    roles.priority.low shouldNotBe Color.Unspecified
    roles.priority.medium shouldNotBe Color.Unspecified
    roles.priority.high shouldNotBe Color.Unspecified
    roles.priority.urgent shouldNotBe Color.Unspecified

    roles.spaceType.personal shouldNotBe Color.Unspecified
    roles.spaceType.shared shouldNotBe Color.Unspecified
    roles.spaceType.system shouldNotBe Color.Unspecified
  }

  @Test
  fun `dark color roles returns valid non-unspecified colors`() {
    val roles = darkDayKeeperColorRoles()

    roles.priority.none shouldNotBe Color.Unspecified
    roles.priority.low shouldNotBe Color.Unspecified
    roles.priority.medium shouldNotBe Color.Unspecified
    roles.priority.high shouldNotBe Color.Unspecified
    roles.priority.urgent shouldNotBe Color.Unspecified

    roles.spaceType.personal shouldNotBe Color.Unspecified
    roles.spaceType.shared shouldNotBe Color.Unspecified
    roles.spaceType.system shouldNotBe Color.Unspecified
  }

  @Test
  fun `calendar palette has expected size`() {
    val roles = lightDayKeeperColorRoles()
    roles.calendar.palette shouldHaveSize EXPECTED_CALENDAR_PALETTE_SIZE
  }

  @Test
  fun `priority colors are all distinct`() {
    val roles = lightDayKeeperColorRoles()
    val colors =
      listOf(
        roles.priority.none,
        roles.priority.low,
        roles.priority.medium,
        roles.priority.high,
        roles.priority.urgent,
      )
    colors.distinct().size shouldBe colors.size
  }

  @Test
  fun `space type colors are all distinct`() {
    val roles = lightDayKeeperColorRoles()
    val colors = listOf(roles.spaceType.personal, roles.spaceType.shared, roles.spaceType.system)
    colors.distinct().size shouldBe colors.size
  }

  @Test
  fun `light and dark priority colors differ`() {
    val light = lightDayKeeperColorRoles()
    val dark = darkDayKeeperColorRoles()

    light.priority shouldNotBe dark.priority
  }

  @Test
  fun `light and dark space type colors differ`() {
    val light = lightDayKeeperColorRoles()
    val dark = darkDayKeeperColorRoles()

    light.spaceType shouldNotBe dark.spaceType
  }

  @Test
  fun `calendar palette is shared between light and dark`() {
    val light = lightDayKeeperColorRoles()
    val dark = darkDayKeeperColorRoles()

    light.calendar shouldBe dark.calendar
  }
}
