package com.jsamuelsen11.daykeeper.feature.profile.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.AccountRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val DEFAULT_TENANT_ID = "default-tenant"

class ProfileOverviewViewModel(
  accountRepository: AccountRepository,
) : ViewModel() {

  val uiState: StateFlow<ProfileOverviewUiState> =
    accountRepository
      .observeById(DEFAULT_TENANT_ID)
      .map<_, ProfileOverviewUiState> { account ->
        if (account != null) {
          ProfileOverviewUiState.Success(
            displayName = account.displayName,
            email = account.email,
          )
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
