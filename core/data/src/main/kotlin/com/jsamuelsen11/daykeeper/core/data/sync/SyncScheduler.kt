package com.jsamuelsen11.daykeeper.core.data.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val SYNC_WORK_NAME = "daykeeper_periodic_sync"
private const val PERIODIC_INTERVAL_MINUTES = 15L
private const val BACKOFF_DELAY_SECONDS = 30L

/** Schedules periodic and on-demand sync via WorkManager. */
class SyncScheduler(private val workManager: WorkManager) {

  private val networkConstraints =
    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

  fun schedulePeriodicSync() {
    val request =
      PeriodicWorkRequestBuilder<SyncWorker>(PERIODIC_INTERVAL_MINUTES, TimeUnit.MINUTES)
        .setConstraints(networkConstraints)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_SECONDS, TimeUnit.SECONDS)
        .build()
    workManager.enqueueUniquePeriodicWork(SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
  }

  fun requestImmediateSync() {
    val request = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(networkConstraints).build()
    workManager.enqueue(request)
  }
}
