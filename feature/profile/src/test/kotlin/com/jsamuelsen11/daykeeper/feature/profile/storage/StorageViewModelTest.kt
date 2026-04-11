package com.jsamuelsen11.daykeeper.feature.profile.storage

import android.app.Application
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.preferences.UserPreferencesRepository
import com.jsamuelsen11.daykeeper.feature.profile.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.profile.makePreferences
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class StorageViewModelTest {

  private val preferencesRepository = mockk<UserPreferencesRepository>()
  private val mockCacheDir = File(System.getProperty("java.io.tmpdir"), "test-cache-dir")
  private val application = mockk<Application> { every { cacheDir } returns mockCacheDir }

  @Test
  fun `emits Success with cache size and max from preferences`() = runTest {
    val prefs = makePreferences()
    every { preferencesRepository.userPreferences } returns flowOf(prefs)

    val viewModel =
      StorageViewModel(application = application, userPreferencesRepository = preferencesRepository)

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<StorageUiState.Success>()
      state.currentCacheSizeMb shouldBe 0L
      state.maxCacheSizeMb shouldBe 100
    }
  }

  @Test
  fun `updateMaxCacheSize calls preferences repository`() = runTest {
    every { preferencesRepository.userPreferences } returns flowOf(makePreferences())
    coEvery { preferencesRepository.setAttachmentCacheSizeMb(200) } just runs

    val viewModel =
      StorageViewModel(application = application, userPreferencesRepository = preferencesRepository)
    viewModel.updateMaxCacheSize(200)

    coVerify { preferencesRepository.setAttachmentCacheSizeMb(200) }
  }
}
