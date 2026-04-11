package com.jsamuelsen11.daykeeper.feature.profile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.preferences.DateFormat
import com.jsamuelsen11.daykeeper.core.data.preferences.DefaultCalendarView
import com.jsamuelsen11.daykeeper.core.data.preferences.ListSortOrder
import com.jsamuelsen11.daykeeper.core.data.preferences.NotificationSound
import com.jsamuelsen11.daykeeper.core.data.preferences.ReminderLeadTime
import com.jsamuelsen11.daykeeper.core.data.preferences.ThemeMode
import com.jsamuelsen11.daykeeper.core.data.preferences.TimeFormat
import com.jsamuelsen11.daykeeper.core.data.preferences.UserPreferencesRepository
import com.jsamuelsen11.daykeeper.core.data.repository.AccountRepository
import com.jsamuelsen11.daykeeper.core.model.account.Account
import com.jsamuelsen11.daykeeper.core.model.account.WeekStart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val DEFAULT_TENANT_ID = "default-tenant"

class AccountSettingsViewModel(
  private val accountRepository: AccountRepository,
  private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

  val uiState: StateFlow<AccountSettingsUiState> =
    combine(
        accountRepository.observeById(DEFAULT_TENANT_ID),
        userPreferencesRepository.userPreferences,
      ) { account, preferences ->
        if (account != null) {
          AccountSettingsUiState.Success(account = account, preferences = preferences)
        } else {
          AccountSettingsUiState.Error("Account not found")
        }
      }
      .catch { e -> emit(AccountSettingsUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        AccountSettingsUiState.Loading,
      )

  fun updateDisplayName(name: String) {
    updateAccount { it.copy(displayName = name, updatedAt = System.currentTimeMillis()) }
  }

  fun updateTimezone(timezone: String) {
    updateAccount { it.copy(timezone = timezone, updatedAt = System.currentTimeMillis()) }
  }

  fun updateWeekStart(weekStart: WeekStart) {
    updateAccount { it.copy(weekStart = weekStart, updatedAt = System.currentTimeMillis()) }
  }

  fun updateThemeMode(mode: ThemeMode) {
    viewModelScope.launch { userPreferencesRepository.setThemeMode(mode) }
  }

  fun updateDefaultCalendarView(view: DefaultCalendarView) {
    viewModelScope.launch { userPreferencesRepository.setDefaultCalendarView(view) }
  }

  fun updateDateFormat(format: DateFormat) {
    viewModelScope.launch { userPreferencesRepository.setDateFormat(format) }
  }

  fun updateTimeFormat(format: TimeFormat) {
    viewModelScope.launch { userPreferencesRepository.setTimeFormat(format) }
  }

  fun updateListSortOrder(order: ListSortOrder) {
    viewModelScope.launch { userPreferencesRepository.setListSortOrder(order) }
  }

  fun updateCompactMode(enabled: Boolean) {
    viewModelScope.launch { userPreferencesRepository.setCompactMode(enabled) }
  }

  fun updateDndEnabled(enabled: Boolean) {
    viewModelScope.launch { userPreferencesRepository.setDndEnabled(enabled) }
  }

  fun updateDndStartTime(time: String) {
    viewModelScope.launch { userPreferencesRepository.setDndStartTime(time) }
  }

  fun updateDndEndTime(time: String) {
    viewModelScope.launch { userPreferencesRepository.setDndEndTime(time) }
  }

  fun updateDefaultReminderLeadTime(leadTime: ReminderLeadTime) {
    viewModelScope.launch { userPreferencesRepository.setDefaultReminderLeadTime(leadTime) }
  }

  fun updateNotificationSound(sound: NotificationSound) {
    viewModelScope.launch { userPreferencesRepository.setNotificationSound(sound) }
  }

  fun updateNotifyEvents(enabled: Boolean) {
    viewModelScope.launch { userPreferencesRepository.setNotifyEvents(enabled) }
  }

  fun updateNotifyTasks(enabled: Boolean) {
    viewModelScope.launch { userPreferencesRepository.setNotifyTasks(enabled) }
  }

  fun updateNotifyLists(enabled: Boolean) {
    viewModelScope.launch { userPreferencesRepository.setNotifyLists(enabled) }
  }

  fun updateNotifyPeople(enabled: Boolean) {
    viewModelScope.launch { userPreferencesRepository.setNotifyPeople(enabled) }
  }

  private fun updateAccount(transform: (Account) -> Account) {
    viewModelScope.launch {
      val current = accountRepository.getById(DEFAULT_TENANT_ID) ?: return@launch
      accountRepository.upsert(transform(current))
    }
  }
}
