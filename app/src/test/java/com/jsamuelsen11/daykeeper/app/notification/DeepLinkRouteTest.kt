package com.jsamuelsen11.daykeeper.app.notification

import com.jsamuelsen11.daykeeper.app.DeepLinkRoute
import com.jsamuelsen11.daykeeper.core.data.notification.DeepLinkConstants
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DeepLinkRouteTest {

  @Test
  fun `DeepLinkRoute holds type and entityId`() {
    val route = DeepLinkRoute(DeepLinkConstants.TYPE_EVENT, TEST_EVENT_ID)

    route.type shouldBe DeepLinkConstants.TYPE_EVENT
    route.entityId shouldBe TEST_EVENT_ID
  }

  @Test
  fun `DeepLinkRoute equality works`() {
    val route1 = DeepLinkRoute(DeepLinkConstants.TYPE_TASK, TEST_TASK_ID)
    val route2 = DeepLinkRoute(DeepLinkConstants.TYPE_TASK, TEST_TASK_ID)

    route1 shouldBe route2
  }

  companion object {
    private const val TEST_EVENT_ID = "event-123"
    private const val TEST_TASK_ID = "task-456"
  }
}
