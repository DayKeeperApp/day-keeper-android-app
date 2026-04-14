package com.jsamuelsen11.daykeeper.feature.profile.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.AccountRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

class ProfileOverviewViewModel(
  accountRepository: AccountRepository,
  sessionProvider: CurrentSessionProvider,
) : ViewModel() {

  val uiState: StateFlow<ProfileOverviewUiState> =
    accountRepository
      .observeById(sessionProvider.tenantId)
      .map<_, ProfileOverviewUiState> { account ->
        if (account != null) {
          ProfileOverviewUiState.Success(displayName = account.displayName, email = account.email)
        } else {
          ProfileOverviewUiState.Success(displayName = "User", email = "")
        }
      }
      .catch { e -> emit(ProfileOverviewUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        ProfileOverviewUiState.Loading,
      )
}
