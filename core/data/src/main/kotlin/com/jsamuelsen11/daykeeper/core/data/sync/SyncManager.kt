package com.jsamuelsen11.daykeeper.core.data.sync

import com.jsamuelsen11.daykeeper.core.data.repository.SyncCursorRepository
import com.jsamuelsen11.daykeeper.core.database.entity.sync.SyncCursorEntity
import com.jsamuelsen11.daykeeper.core.database.sync.SyncDao
import com.jsamuelsen11.daykeeper.core.model.sync.SyncCursor
import com.jsamuelsen11.daykeeper.core.network.api.SyncApi
import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPullRequestDto
import com.jsamuelsen11.daykeeper.core.network.dto.sync.SyncPushRequestDto
import com.jsamuelsen11.daykeeper.core.network.mapper.PullResponseParser
import com.jsamuelsen11.daykeeper.core.network.mapper.SyncEntryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val SYNC_CURSOR_ID = "sync_cursor"

/**
 * Orchestrates the full sync cycle: push local changes, pull remote changes, apply to local DB.
 *
 * Thread-safe via [Mutex] — concurrent [requestSync] calls are coalesced.
 */
class SyncManager(
  private val syncApi: SyncApi,
  private val syncDao: SyncDao,
  private val syncCursorRepository: SyncCursorRepository,
  private val scope: CoroutineScope,
) : SyncStatusProvider {

  private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
  override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

  private val mutex = Mutex()

  override fun requestSync() {
    scope.launch { sync() }
  }

  /** Runs a full push-then-pull sync cycle. Returns success/failure. */
  suspend fun sync(): Result<Unit> =
    mutex.withLock {
      _syncStatus.value = SyncStatus.Syncing
      runCatching { executeSyncCycle() }
        .onSuccess { _syncStatus.value = SyncStatus.Idle }
        .onFailure { _syncStatus.value = SyncStatus.Error(it.message ?: "Sync failed") }
    }

  private suspend fun executeSyncCycle() {
    val cursor = syncCursorRepository.getCursor()
    val lastSyncAt = cursor?.lastSyncAt ?: 0L
    val lastCursor = cursor?.lastCursor ?: 0L

    // 1. Push local changes
    val changedEntities = syncDao.getChangedEntities(lastSyncAt)
    val pushEntries = SyncEntryBuilder.buildPushEntries(changedEntities)
    if (pushEntries.isNotEmpty()) {
      syncApi.push(SyncPushRequestDto(pushEntries))
    }

    // 2. Pull remote changes (paginated)
    var pullCursor = lastCursor
    do {
      val pullResponse = syncApi.pull(SyncPullRequestDto(cursor = pullCursor))
      if (pullResponse.changes.isNotEmpty()) {
        val pulledChanges = PullResponseParser.parse(pullResponse.changes)
        val cursorEntity = SyncCursorEntity(SYNC_CURSOR_ID, pullResponse.cursor, lastSyncAt)
        syncDao.applyPulledChanges(pulledChanges, cursorEntity)
      }
      pullCursor = pullResponse.cursor
    } while (pullResponse.hasMore)

    // 3. Update cursor with final state
    val now = System.currentTimeMillis()
    syncCursorRepository.upsert(SyncCursor(lastCursor = pullCursor, lastSyncAt = now))
  }
}
