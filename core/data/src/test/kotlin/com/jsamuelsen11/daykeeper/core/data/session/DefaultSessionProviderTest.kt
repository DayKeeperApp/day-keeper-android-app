package com.jsamuelsen11.daykeeper.core.data.session

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DefaultSessionProviderTest {

  private val provider = DefaultSessionProvider()

  @Test
  fun `tenantId returns default tenant`() {
    provider.tenantId shouldBe "default-tenant"
  }

  @Test
  fun `spaceId returns default space`() {
    provider.spaceId shouldBe "default-space"
  }

  @Test
  fun `currentTenantId StateFlow value matches tenantId`() {
    provider.currentTenantId.value shouldBe provider.tenantId
  }

  @Test
  fun `currentSpaceId StateFlow value matches spaceId`() {
    provider.currentSpaceId.value shouldBe provider.spaceId
  }
}
