package com.jsamuelsen11.daykeeper.feature.calendar.management

private const val DEFAULT_CALENDAR_COLOR = "#4285F4"

/** UI state for the calendar create/edit screen. */
sealed interface CalendarCreateEditUiState {
  /** Initial state while an existing calendar is loading. */
  data object Loading : CalendarCreateEditUiState

  /**
   * Form is ready for interaction.
   *
   * @property name Current value of the name field.
   * @property color Current hex color string (e.g. `"#4285F4"`).
   * @property isDefault Whether this calendar is marked as the default.
   * @property isEditing Whether the screen is editing an existing calendar.
   * @property isSaving Whether a save operation is currently in progress.
   * @property nameError Validation error for the name field, or null if valid.
   */
  data class Ready(
    val name: String = "",
    val color: String = DEFAULT_CALENDAR_COLOR,
    val isDefault: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null,
  ) : CalendarCreateEditUiState
}

/** One-shot events emitted by [CalendarCreateEditViewModel]. */
sealed interface CalendarCreateEditEvent {
  /** Emitted after the calendar has been successfully saved. */
  data object Saved : CalendarCreateEditEvent
}
