package com.jsamuelsen11.daykeeper.core.network.api

import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPullRequestDto
import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPullResponseDto
import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPushRequestDto
import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPushResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

private const val SYNC_PUSH_PATH = "api/v1/Sync/push"
private const val SYNC_PULL_PATH = "api/v1/Sync/pull"

class SyncApiImpl(private val httpClient: HttpClient) : SyncApi {

  override suspend fun push(request: SyncPushRequestDto): SyncPushResponseDto =
    httpClient.post(SYNC_PUSH_PATH) { setBody(request) }.body()

  override suspend fun pull(request: SyncPullRequestDto): SyncPullResponseDto =
    httpClient.post(SYNC_PULL_PATH) { setBody(request) }.body()
}
