package com.jsamuelsen11.daykeeper.feature.profile.settings

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.preferences.ThemeMode
import com.jsamuelsen11.daykeeper.core.data.preferences.UserPreferencesRepository
import com.jsamuelsen11.daykeeper.core.data.repository.AccountRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import com.jsamuelsen11.daykeeper.core.model.account.WeekStart
import com.jsamuelsen11.daykeeper.feature.profile.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.profile.TEST_TENANT_ID
import com.jsamuelsen11.daykeeper.feature.profile.makeAccount
import com.jsamuelsen11.daykeeper.feature.profile.makePreferences
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class AccountSettingsViewModelTest {

  private val accountRepository = mockk<AccountRepository>()
  private val preferencesRepository = mockk<UserPreferencesRepository>()
  private val sessionProvider = mockk<CurrentSessionProvider>()

  @BeforeEach
  fun setUp() {
    every { sessionProvider.tenantId } returns TEST_TENANT_ID
    every { accountRepository.observeById(TEST_TENANT_ID) } returns flowOf(makeAccount())
    every { preferencesRepository.userPreferences } returns flowOf(makePreferences())
  }

  private fun createViewModel(): AccountSettingsViewModel =
    AccountSettingsViewModel(
      accountRepository = accountRepository,
      userPreferencesRepository = preferencesRepository,
      sessionProvider = sessionProvider,
    )

  @Test
  fun `initial state combines account and preferences`() = runTest {
    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<AccountSettingsUiState.Success>()
      state.account.displayName shouldBe "Test User"
      state.account.timezone shouldBe "America/New_York"
      state.preferences.compactMode shouldBe false
    }
  }

  @Test
  fun `emits Error when account is null`() = runTest {
    every { accountRepository.observeById(TEST_TENANT_ID) } returns flowOf(null)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<AccountSettingsUiState.Error>()
      state.message shouldBe "Account not found"
    }
  }

  @Test
  fun `updateWeekStart calls upsert with new value`() = runTest {
    val account = makeAccount()
    coEvery { accountRepository.getById(TEST_TENANT_ID) } returns account
    coEvery { accountRepository.upsert(any()) } just runs

    val viewModel = createViewModel()
    viewModel.updateWeekStart(WeekStart.MONDAY)

    coVerify { accountRepository.upsert(match { it.weekStart == WeekStart.MONDAY }) }
  }

  @Test
  fun `updateThemeMode calls preferences repository`() = runTest {
    coEvery { preferencesRepository.setThemeMode(any()) } just runs

    val viewModel = createViewModel()
    viewModel.updateThemeMode(ThemeMode.DARK)

    coVerify { preferencesRepository.setThemeMode(ThemeMode.DARK) }
  }
}
