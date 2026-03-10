package com.jsamuelsen11.daykeeper.core.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Task
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Centralized icon catalog for the Day Keeper app.
 *
 * All feature modules should reference icons through this object rather than importing Material
 * Icons directly. This ensures consistent icon usage across the app and makes it easy to swap icons
 * in one place.
 */
object DayKeeperIcons {

  // region Navigation (bottom bar)
  val Calendar: ImageVector = Icons.Outlined.CalendarMonth
  val Tasks: ImageVector = Icons.Outlined.CheckCircle
  val Lists: ImageVector = Icons.Outlined.Checklist
  val People: ImageVector = Icons.Outlined.People
  val Profile: ImageVector = Icons.Outlined.Person
  // endregion

  // region Actions
  val Add: ImageVector = Icons.Outlined.Add
  val Edit: ImageVector = Icons.Outlined.Edit
  val Delete: ImageVector = Icons.Outlined.Delete
  val Check: ImageVector = Icons.Outlined.Check
  val Close: ImageVector = Icons.Outlined.Close
  val Search: ImageVector = Icons.Outlined.Search
  val MoreVert: ImageVector = Icons.Outlined.MoreVert
  val Sort: ImageVector = Icons.AutoMirrored.Outlined.Sort
  val FilterList: ImageVector = Icons.Outlined.FilterList
  val Share: ImageVector = Icons.Outlined.Share
  // endregion

  // region Navigation chrome
  val ArrowBack: ImageVector = Icons.AutoMirrored.Outlined.ArrowBack
  val ChevronRight: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight
  val ExpandMore: ImageVector = Icons.Outlined.ExpandMore
  val ExpandLess: ImageVector = Icons.Outlined.ExpandLess
  val Menu: ImageVector = Icons.Outlined.Menu
  // endregion

  // region Calendar & Events
  val Event: ImageVector = Icons.Outlined.Event
  val Location: ImageVector = Icons.Outlined.LocationOn
  val Notification: ImageVector = Icons.Outlined.Notifications
  val Repeat: ImageVector = Icons.Outlined.Repeat
  val Schedule: ImageVector = Icons.Outlined.Schedule
  // endregion

  // region People
  val Phone: ImageVector = Icons.Outlined.Phone
  val Email: ImageVector = Icons.Outlined.Email
  val Home: ImageVector = Icons.Outlined.Home
  val Cake: ImageVector = Icons.Outlined.Cake
  val Map: ImageVector = Icons.Outlined.Map
  // endregion

  // region Tasks & Projects
  val Task: ImageVector = Icons.Outlined.Task
  val Project: ImageVector = Icons.Outlined.Folder
  // endregion

  // region Lists
  val ShoppingCart: ImageVector = Icons.Outlined.ShoppingCart
  // endregion

  // region Media & Attachments
  val Camera: ImageVector = Icons.Outlined.PhotoCamera
  val Attachment: ImageVector = Icons.Outlined.AttachFile
  // endregion

  // region Settings & System
  val Settings: ImageVector = Icons.Outlined.Settings
  val Sync: ImageVector = Icons.Outlined.Sync
  val StorageIcon: ImageVector = Icons.Outlined.Storage
  val Info: ImageVector = Icons.Outlined.Info
  // endregion
}
