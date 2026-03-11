package com.jsamuelsen11.daykeeper.core.ui.icon

import androidx.compose.ui.graphics.vector.ImageVector
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContainDuplicates
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class DayKeeperIconsTest {

  private val allIcons: List<ImageVector> =
    listOf(
      // Navigation
      DayKeeperIcons.Calendar,
      DayKeeperIcons.Tasks,
      DayKeeperIcons.Lists,
      DayKeeperIcons.People,
      DayKeeperIcons.Profile,
      // Actions
      DayKeeperIcons.Add,
      DayKeeperIcons.Edit,
      DayKeeperIcons.Delete,
      DayKeeperIcons.Check,
      DayKeeperIcons.Close,
      DayKeeperIcons.Search,
      DayKeeperIcons.MoreVert,
      DayKeeperIcons.Sort,
      DayKeeperIcons.FilterList,
      DayKeeperIcons.Share,
      DayKeeperIcons.DragHandle,
      // Navigation chrome
      DayKeeperIcons.ArrowBack,
      DayKeeperIcons.ChevronRight,
      DayKeeperIcons.ExpandMore,
      DayKeeperIcons.ExpandLess,
      DayKeeperIcons.Menu,
      // Calendar & Events
      DayKeeperIcons.Event,
      DayKeeperIcons.Location,
      DayKeeperIcons.Notification,
      DayKeeperIcons.Repeat,
      DayKeeperIcons.Schedule,
      // People
      DayKeeperIcons.Phone,
      DayKeeperIcons.Email,
      DayKeeperIcons.Home,
      DayKeeperIcons.Cake,
      DayKeeperIcons.Map,
      // Tasks & Projects
      DayKeeperIcons.Task,
      DayKeeperIcons.Project,
      // Lists
      DayKeeperIcons.ShoppingCart,
      // Media & Attachments
      DayKeeperIcons.Camera,
      DayKeeperIcons.Attachment,
      // Settings & System
      DayKeeperIcons.Settings,
      DayKeeperIcons.Sync,
      DayKeeperIcons.StorageIcon,
      DayKeeperIcons.Info,
    )

  private val navigationIcons: List<ImageVector> =
    listOf(
      DayKeeperIcons.Calendar,
      DayKeeperIcons.Tasks,
      DayKeeperIcons.Lists,
      DayKeeperIcons.People,
      DayKeeperIcons.Profile,
    )

  @Test
  fun `icon catalog contains expected number of icons`() {
    val expectedCount = 40
    allIcons shouldHaveSize expectedCount
  }

  @Test
  fun `all icons have a non-blank name`() {
    allIcons.forEach { icon -> icon.name.shouldNotBeBlank() }
  }

  @Test
  fun `all icons are valid ImageVector instances`() {
    allIcons.forEach { icon -> icon shouldNotBe null }
  }

  @Test
  fun `navigation icons are all distinct`() {
    navigationIcons.map { it.name }.shouldNotContainDuplicates()
  }

  @Test
  fun `bottom navigation has exactly five icons`() {
    navigationIcons shouldHaveSize 5
  }
}
