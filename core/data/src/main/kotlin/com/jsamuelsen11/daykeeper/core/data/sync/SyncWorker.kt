package com.jsamuelsen11.daykeeper.core.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

private const val MAX_RETRY_ATTEMPTS = 3

/** WorkManager worker that delegates to [SyncManager] for background sync. */
class SyncWorker(context: Context, params: WorkerParameters, private val syncManager: SyncManager) :
  CoroutineWorker(context, params) {

  override suspend fun doWork(): Result =
    syncManager
      .sync()
      .fold(
        onSuccess = { Result.success() },
        onFailure = {
          if (runAttemptCount < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        },
      )
}
