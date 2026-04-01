package com.jsamuelsen11.daykeeper.core.data.notification

/** Constants for notification deep link intent extras. */
public object DeepLinkConstants {
  public const val EXTRA_DEEP_LINK_TYPE: String = "deep_link_type"
  public const val EXTRA_ENTITY_ID: String = "entity_id"
  public const val TYPE_EVENT: String = "event"
  public const val TYPE_TASK: String = "task"
}
