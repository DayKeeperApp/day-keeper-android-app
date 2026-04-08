package com.jsamuelsen11.daykeeper.feature.profile.settings

import com.jsamuelsen11.daykeeper.core.data.preferences.UserPreferences
import com.jsamuelsen11.daykeeper.core.model.account.Account

sealed interface AccountSettingsUiState {
  data object Loading : AccountSettingsUiState

  data class Success(
    val account: Account,
    val preferences: UserPreferences,
  ) : AccountSettingsUiState

  data class Error(val message: String) : AccountSettingsUiState
}
