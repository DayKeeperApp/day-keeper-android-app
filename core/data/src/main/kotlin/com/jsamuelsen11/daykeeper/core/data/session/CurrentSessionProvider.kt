package com.jsamuelsen11.daykeeper.core.data.session

import kotlinx.coroutines.flow.StateFlow

/** Provides the active tenant and space identifiers for the current session. */
interface CurrentSessionProvider {
  /** Observable tenant ID for reactive pipelines that respond to tenant switches. */
  val currentTenantId: StateFlow<String>

  /** Observable space ID for reactive pipelines that respond to space switches. */
  val currentSpaceId: StateFlow<String>

  /** Snapshot of the current tenant ID. Equivalent to `currentTenantId.value`. */
  val tenantId: String

  /** Snapshot of the current space ID. Equivalent to `currentSpaceId.value`. */
  val spaceId: String
}
