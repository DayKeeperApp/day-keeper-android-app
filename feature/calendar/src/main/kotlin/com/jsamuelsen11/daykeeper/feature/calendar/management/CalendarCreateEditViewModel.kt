package com.jsamuelsen11.daykeeper.feature.calendar.management

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar
import com.jsamuelsen11.daykeeper.feature.calendar.navigation.CalendarCreateEditRoute
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the calendar create/edit screen.
 *
 * Determines create-vs-edit mode from the optional `calendarId` in [SavedStateHandle]. Emits
 * [CalendarCreateEditEvent.Saved] through [events] when a save succeeds so the screen can navigate
 * away.
 *
 * @param savedStateHandle Navigation back-stack handle.
 * @param calendarRepository Source of truth for calendar data.
 */
class CalendarCreateEditViewModel(
  savedStateHandle: SavedStateHandle,
  private val calendarRepository: CalendarRepository,
  private val sessionProvider: CurrentSessionProvider,
) : ViewModel() {

  private val calendarId: String? = savedStateHandle.toRoute<CalendarCreateEditRoute>().calendarId

  private val _uiState: MutableStateFlow<CalendarCreateEditUiState> =
    MutableStateFlow(CalendarCreateEditUiState.Loading)

  /** The current UI state. Consumers should use [collectAsStateWithLifecycle]. */
  val uiState: StateFlow<CalendarCreateEditUiState> = _uiState.asStateFlow()

  private val _events: Channel<CalendarCreateEditEvent> = Channel(Channel.BUFFERED)

  /** One-shot events to be consumed by the screen. */
  val events = _events.receiveAsFlow()

  init {
    if (calendarId != null) {
      viewModelScope.launch {
        val existing = calendarRepository.getById(calendarId)
        _uiState.value =
          if (existing != null) {
            CalendarCreateEditUiState.Ready(
              name = existing.name,
              color = existing.color,
              isDefault = existing.isDefault,
              isEditing = true,
            )
          } else {
            CalendarCreateEditUiState.Ready(isEditing = true)
          }
      }
    } else {
      _uiState.value = CalendarCreateEditUiState.Ready(isEditing = false)
    }
  }

  /**
   * Updates the calendar name field and clears any existing name validation error.
   *
   * @param name The new name string entered by the user.
   */
  fun onNameChanged(name: String) {
    _uiState.update { current ->
      (current as? CalendarCreateEditUiState.Ready)?.copy(name = name, nameError = null) ?: current
    }
  }

  /**
   * Selects a new color for the calendar.
   *
   * @param color A hex color string, e.g. `"#4285F4"`.
   */
  fun onColorSelected(color: String) {
    _uiState.update { current ->
      (current as? CalendarCreateEditUiState.Ready)?.copy(color = color) ?: current
    }
  }

  /** Toggles whether this calendar is the default calendar in its space. */
  fun onDefaultToggled(isDefault: Boolean) {
    _uiState.update { current ->
      (current as? CalendarCreateEditUiState.Ready)?.copy(isDefault = isDefault) ?: current
    }
  }

  /**
   * Validates inputs and persists the calendar.
   *
   * The name must be non-blank. On success the calendar is upserted and
   * [CalendarCreateEditEvent.Saved] is emitted. On failure
   * [CalendarCreateEditUiState.Ready.nameError] is set.
   */
  fun onSave() {
    val current = _uiState.value as? CalendarCreateEditUiState.Ready ?: return

    if (current.name.isBlank()) {
      _uiState.update { state ->
        (state as? CalendarCreateEditUiState.Ready)?.copy(nameError = NAME_REQUIRED_ERROR) ?: state
      }
      return
    }

    _uiState.update { state ->
      (state as? CalendarCreateEditUiState.Ready)?.copy(isSaving = true) ?: state
    }

    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val existing = calendarId?.let { calendarRepository.getById(it) }

      val calendar =
        existing?.copy(
          name = current.name.trim(),
          normalizedName = current.name.trim().lowercase(),
          color = current.color,
          isDefault = current.isDefault,
          updatedAt = now,
        )
          ?: Calendar(
            calendarId = UUID.randomUUID().toString(),
            spaceId = sessionProvider.spaceId,
            tenantId = sessionProvider.tenantId,
            name = current.name.trim(),
            normalizedName = current.name.trim().lowercase(),
            color = current.color,
            isDefault = current.isDefault,
            createdAt = now,
            updatedAt = now,
          )

      runCatching { calendarRepository.upsert(calendar) }
        .onSuccess {
          _uiState.update { state ->
            (state as? CalendarCreateEditUiState.Ready)?.copy(isSaving = false) ?: state
          }
          _events.send(CalendarCreateEditEvent.Saved)
        }
        .onFailure { error ->
          _uiState.update { state ->
            (state as? CalendarCreateEditUiState.Ready)?.copy(
              isSaving = false,
              nameError = error.message ?: SAVE_FAILED_ERROR,
            ) ?: state
          }
        }
    }
  }

  companion object {
    internal const val NAME_REQUIRED_ERROR = "Name is required"
    internal const val SAVE_FAILED_ERROR = "Save failed"
    internal const val STOP_TIMEOUT_MILLIS = 5_000L
  }
}
