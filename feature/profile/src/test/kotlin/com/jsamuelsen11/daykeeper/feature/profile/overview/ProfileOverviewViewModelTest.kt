package com.jsamuelsen11.daykeeper.feature.profile.overview

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.AccountRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import com.jsamuelsen11.daykeeper.feature.profile.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.profile.TEST_TENANT_ID
import com.jsamuelsen11.daykeeper.feature.profile.makeAccount
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class ProfileOverviewViewModelTest {

  private val accountRepository = mockk<AccountRepository>()
  private val sessionProvider =
    mockk<CurrentSessionProvider> { every { tenantId } returns TEST_TENANT_ID }

  private fun createViewModel(): ProfileOverviewViewModel =
    ProfileOverviewViewModel(
      accountRepository = accountRepository,
      sessionProvider = sessionProvider,
    )

  @Test
  fun `emits Success with account data when account exists`() = runTest {
    val account = makeAccount(displayName = "John Doe", email = "john@example.com")
    every { accountRepository.observeById(TEST_TENANT_ID) } returns flowOf(account)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<ProfileOverviewUiState.Success>()
      state.displayName shouldBe "John Doe"
      state.email shouldBe "john@example.com"
    }
  }

  @Test
  fun `emits Success with default name when account is null`() = runTest {
    every { accountRepository.observeById(TEST_TENANT_ID) } returns flowOf(null)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<ProfileOverviewUiState.Success>()
      state.displayName shouldBe "User"
      state.email shouldBe ""
    }
  }

  @Test
  fun `emits Error when repository throws`() = runTest {
    every { accountRepository.observeById(TEST_TENANT_ID) } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException("DB error") }

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<ProfileOverviewUiState.Error>()
      state.message shouldBe "DB error"
    }
  }
}
