package com.jsamuelsen11.daykeeper.core.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DefaultSessionProvider : CurrentSessionProvider {
  private val _currentTenantId = MutableStateFlow(DEFAULT_TENANT_ID)
  private val _currentSpaceId = MutableStateFlow(DEFAULT_SPACE_ID)

  override val currentTenantId: StateFlow<String> = _currentTenantId.asStateFlow()
  override val currentSpaceId: StateFlow<String> = _currentSpaceId.asStateFlow()

  override val tenantId: String
    get() = _currentTenantId.value

  override val spaceId: String
    get() = _currentSpaceId.value

  internal companion object {
    internal const val DEFAULT_TENANT_ID = "default-tenant"
    internal const val DEFAULT_SPACE_ID = "default-space"
  }
}
