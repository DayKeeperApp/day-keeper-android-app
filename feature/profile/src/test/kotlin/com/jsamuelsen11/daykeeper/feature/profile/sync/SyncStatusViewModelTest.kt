package com.jsamuelsen11.daykeeper.feature.profile.sync

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.SyncCursorRepository
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatus
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatusProvider
import com.jsamuelsen11.daykeeper.feature.profile.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.profile.makeSyncCursor
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class SyncStatusViewModelTest {

  private val syncStatusFlow = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
  private val syncStatusProvider =
    mockk<SyncStatusProvider> { every { syncStatus } returns syncStatusFlow }
  private val syncCursorRepository = mockk<SyncCursorRepository>()

  @Test
  fun `shows idle status with last sync time`() = runTest {
    val cursor = makeSyncCursor(lastSyncAt = 1_700_000_000_000L)
    every { syncCursorRepository.observeCursor() } returns flowOf(cursor)

    val viewModel =
      SyncStatusViewModel(
        syncStatusProvider = syncStatusProvider,
        syncCursorRepository = syncCursorRepository,
      )

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<SyncStatusUiState.Success>()
      state.status shouldBe "Idle"
      state.isSyncing shouldBe false
    }
  }

  @Test
  fun `shows syncing status`() = runTest {
    every { syncCursorRepository.observeCursor() } returns flowOf(null)
    syncStatusFlow.value = SyncStatus.Syncing

    val viewModel =
      SyncStatusViewModel(
        syncStatusProvider = syncStatusProvider,
        syncCursorRepository = syncCursorRepository,
      )

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<SyncStatusUiState.Success>()
      state.status shouldBe "Syncing..."
      state.isSyncing shouldBe true
      state.lastSyncFormatted shouldBe "Never"
    }
  }

  @Test
  fun `syncNow triggers requestSync`() = runTest {
    every { syncCursorRepository.observeCursor() } returns flowOf(null)
    every { syncStatusProvider.requestSync() } just runs

    val viewModel =
      SyncStatusViewModel(
        syncStatusProvider = syncStatusProvider,
        syncCursorRepository = syncCursorRepository,
      )

    viewModel.syncNow()

    verify { syncStatusProvider.requestSync() }
  }
}
