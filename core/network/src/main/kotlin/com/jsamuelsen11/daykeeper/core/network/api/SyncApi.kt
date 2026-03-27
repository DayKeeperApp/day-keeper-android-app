package com.jsamuelsen11.daykeeper.core.network.api

import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPullRequestDto
import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPullResponseDto
import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPushRequestDto
import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPushResponseDto

/** REST API client for the sync push/pull endpoints. */
interface SyncApi {
  suspend fun push(request: SyncPushRequestDto): SyncPushResponseDto

  suspend fun pull(request: SyncPullRequestDto): SyncPullResponseDto
}
