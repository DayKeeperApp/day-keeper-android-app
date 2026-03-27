package com.jsamuelsen11.daykeeper.core.network.dto.sync

import kotlinx.serialization.Serializable

/** Request body for POST /api/v1/Sync/push. */
@Serializable data class SyncPushRequestDto(val changes: List<SyncPushEntryDto>)
