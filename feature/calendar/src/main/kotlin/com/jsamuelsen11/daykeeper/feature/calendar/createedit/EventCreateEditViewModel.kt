package com.jsamuelsen11.daykeeper.feature.calendar.createedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.attachment.AttachmentManager
import com.jsamuelsen11.daykeeper.core.data.repository.AttachmentRepository
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventReminderRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventTypeRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachableEntityType
import com.jsamuelsen11.daykeeper.core.model.attachment.Attachment
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import com.jsamuelsen11.daykeeper.core.model.calendar.RecurrenceRule
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MINUTES_PER_HOUR = 60
private const val MILLIS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val DEFAULT_EVENT_DURATION_HOURS = 1L

/**
 * ViewModel for the event create/edit screen.
 *
 * When [SavedStateHandle] contains an `eventId`, the screen enters editing mode and pre-populates
 * the form from the existing event. An optional `calendarId` pre-selects the target calendar for
 * new events; `initialDateMillis` seeds the start time when creating from a specific date.
 *
 * @param savedStateHandle Navigation back-stack handle.
 * @param eventRepository Source of truth for event data.
 * @param calendarRepository Source of truth for calendar data.
 * @param eventTypeRepository Source of truth for event type data.
 * @param eventReminderRepository Source of truth for reminder data.
 * @param attachmentRepository Source of truth for attachment data.
 * @param attachmentManager Manages download, upload, and local cache of attachments.
 */
class EventCreateEditViewModel(
  savedStateHandle: SavedStateHandle,
  private val eventRepository: EventRepository,
  private val calendarRepository: CalendarRepository,
  private val eventTypeRepository: EventTypeRepository,
  private val eventReminderRepository: EventReminderRepository,
  private val attachmentRepository: AttachmentRepository,
  private val attachmentManager: AttachmentManager,
  private val sessionProvider: CurrentSessionProvider,
) : ViewModel() {

  private val eventId: String? = savedStateHandle[KEY_EVENT_ID]
  private val initialDateMillis: Long? = savedStateHandle[KEY_INITIAL_DATE_MILLIS]
  private val isEditing = eventId != null

  private val _uiState = MutableStateFlow<EventCreateEditUiState>(EventCreateEditUiState.Loading)

  /** The current UI state. Consumers should use [collectAsStateWithLifecycle]. */
  val uiState: StateFlow<EventCreateEditUiState> = _uiState.asStateFlow()

  private val _events = Channel<EventCreateEditEvent>(Channel.BUFFERED)

  /** One-shot events. Collect with [LaunchedEffect] in the composable. */
  val events = _events.receiveAsFlow()

  init {
    viewModelScope.launch {
      val existingEvent = if (isEditing) eventRepository.getById(eventId!!) else null
      val existingReminders =
        if (isEditing && existingEvent != null) {
          eventReminderRepository.getById(existingEvent.eventId)?.let { listOf(it) } ?: emptyList()
        } else {
          emptyList<EventReminder>()
        }

      combine(
          calendarRepository.observeBySpace(sessionProvider.spaceId),
          eventTypeRepository.observeAll(),
        ) { calendars, eventTypes ->
          val activeCalendars = calendars.filter { it.deletedAt == null }
          val current = _uiState.value
          when {
            current is EventCreateEditUiState.Loading -> {
              val formState =
                if (existingEvent != null) {
                  EventFormState(
                    title = existingEvent.title,
                    description = existingEvent.description.orEmpty(),
                    calendarId = existingEvent.calendarId,
                    isAllDay = existingEvent.isAllDay,
                    startDate = existingEvent.startDate,
                    endDate = existingEvent.endDate,
                    startAt = existingEvent.startAt,
                    endAt = existingEvent.endAt,
                    timezone = existingEvent.timezone,
                    eventTypeId = existingEvent.eventTypeId,
                    location = existingEvent.location,
                    recurrenceRule = existingEvent.recurrenceRule,
                    reminders =
                      existingReminders
                        .filter { it.deletedAt == null }
                        .map { reminder ->
                          ReminderEntry(
                            id = reminder.reminderId,
                            minutesBefore = reminder.minutesBefore,
                          )
                        },
                  )
                } else {
                  buildDefaultFormState(activeCalendars)
                }
              EventCreateEditUiState.Ready(
                formState = formState,
                isEditing = isEditing,
                calendars = activeCalendars,
                eventTypes = eventTypes,
              )
            }
            current is EventCreateEditUiState.Ready ->
              current.copy(calendars = activeCalendars, eventTypes = eventTypes)
            else -> current
          }
        }
        .collect { newState -> _uiState.value = newState }
    }

    if (isEditing) {
      viewModelScope.launch {
        attachmentRepository.observeByEntity(AttachableEntityType.EVENT, eventId!!).collect {
          attachments ->
          _uiState.update { state ->
            if (state is EventCreateEditUiState.Ready) {
              state.copy(attachments = attachments.map { it.toUiItem() })
            } else {
              state
            }
          }
        }
      }
    }
  }

  private fun buildDefaultFormState(
    calendars: List<com.jsamuelsen11.daykeeper.core.model.calendar.Calendar>
  ): EventFormState {
    val defaultCalendar = calendars.firstOrNull { it.isDefault } ?: calendars.firstOrNull()
    val startMillis = resolveDefaultStartMillis()
    val endMillis =
      startMillis +
        DEFAULT_EVENT_DURATION_HOURS * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLIS_PER_SECOND
    return EventFormState(
      calendarId = defaultCalendar?.calendarId,
      startAt = startMillis,
      endAt = endMillis,
      timezone = TimeZone.getDefault().id,
    )
  }

  private fun resolveDefaultStartMillis(): Long {
    val base = initialDateMillis ?: System.currentTimeMillis()
    val instant = Instant.ofEpochMilli(base)
    val zdt = instant.atZone(ZoneId.systemDefault())
    val nextHour = zdt.truncatedTo(ChronoUnit.HOURS).plusHours(1)
    return nextHour.toInstant().toEpochMilli()
  }

  /** Updates the title and clears any existing title validation error. */
  fun onTitleChanged(title: String) {
    updateForm { it.copy(title = title, titleError = null) }
  }

  /** Updates the description field. */
  fun onDescriptionChanged(description: String) {
    updateForm { it.copy(description = description) }
  }

  /** Selects the calendar by [calendarId]. */
  fun onCalendarSelected(calendarId: String?) {
    updateForm { it.copy(calendarId = calendarId) }
  }

  /** Selects the event type by [eventTypeId], or clears it when null. */
  fun onEventTypeSelected(eventTypeId: String?) {
    updateForm { it.copy(eventTypeId = eventTypeId) }
  }

  /**
   * Toggles the all-day flag. When switching to all-day the time fields are cleared; when switching
   * to timed the date strings are cleared.
   */
  fun onAllDayToggled(isAllDay: Boolean) {
    updateForm { state ->
      if (isAllDay) {
        state.copy(isAllDay = true, startAt = null, endAt = null)
      } else {
        val startMillis = resolveDefaultStartMillis()
        val endMillis =
          startMillis +
            DEFAULT_EVENT_DURATION_HOURS * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLIS_PER_SECOND
        state.copy(
          isAllDay = false,
          startDate = null,
          endDate = null,
          startAt = startMillis,
          endAt = endMillis,
        )
      }
    }
  }

  /**
   * Stores the all-day start date as an ISO-8601 string and clears any date error.
   *
   * @param dateString ISO-8601 date string, e.g. `"2026-03-19"`.
   */
  fun onStartDateSelected(dateString: String) {
    updateForm { it.copy(startDate = dateString, dateError = null) }
  }

  /**
   * Stores the start time as epoch millis.
   *
   * @param epochMillis Epoch millis for the combined date and time instant.
   */
  fun onStartTimeSelected(epochMillis: Long) {
    updateForm { it.copy(startAt = epochMillis, dateError = null) }
  }

  /**
   * Stores the all-day end date as an ISO-8601 string and clears any date error.
   *
   * @param dateString ISO-8601 date string, e.g. `"2026-03-19"`.
   */
  fun onEndDateSelected(dateString: String) {
    updateForm { it.copy(endDate = dateString, dateError = null) }
  }

  /**
   * Stores the end time as epoch millis.
   *
   * @param epochMillis Epoch millis for the combined date and time instant.
   */
  fun onEndTimeSelected(epochMillis: Long) {
    updateForm { it.copy(endAt = epochMillis, dateError = null) }
  }

  /** Updates the location field. */
  fun onLocationChanged(location: String) {
    updateForm { it.copy(location = location.trim().ifBlank { null }) }
  }

  /** Updates the recurrence rule. Pass null to remove recurrence. */
  fun onRecurrenceChanged(rule: RecurrenceRule?) {
    updateForm { it.copy(recurrenceRule = rule) }
  }

  /**
   * Adds a new reminder with the given [minutesBefore] offset. No-ops when a reminder with the same
   * offset already exists.
   */
  fun onAddReminder(minutesBefore: Int) {
    updateForm { state ->
      if (state.reminders.any { it.minutesBefore == minutesBefore }) {
        state
      } else {
        state.copy(
          reminders =
            state.reminders +
              ReminderEntry(id = UUID.randomUUID().toString(), minutesBefore = minutesBefore)
        )
      }
    }
  }

  /** Removes the reminder identified by [reminderId] from the form. */
  fun onRemoveReminder(reminderId: String) {
    updateForm { state -> state.copy(reminders = state.reminders.filter { it.id != reminderId }) }
  }

  /**
   * Validates the form and persists the event together with its reminders.
   *
   * Validation rules:
   * - Title must be non-blank.
   * - A calendar must be selected.
   * - For timed events: end time must be greater than or equal to start time.
   * - For all-day events: end date must be on or after start date.
   *
   * On success emits [EventCreateEditEvent.Saved]. On failure sets the appropriate error field.
   */
  fun onSave() {
    val state =
      (_uiState.value as? EventCreateEditUiState.Ready)?.takeIf { !it.formState.isSaving } ?: return

    val form = state.formState
    val trimmedTitle = form.title.trim()
    if (applyValidationErrors(trimmedTitle, form)) return

    updateForm { it.copy(isSaving = true) }

    viewModelScope.launch {
      runCatching { persistEvent(trimmedTitle, form) }
        .onSuccess { _events.send(EventCreateEditEvent.Saved) }
        .onFailure { error ->
          updateForm { it.copy(isSaving = false, titleError = error.message ?: SAVE_FAILED_ERROR) }
        }
    }
  }

  /** Returns true if any validation error was found and applied to the form state. */
  private fun applyValidationErrors(trimmedTitle: String, form: EventFormState): Boolean {
    val titleError =
      when {
        trimmedTitle.isBlank() -> TITLE_EMPTY_ERROR
        form.calendarId == null -> CALENDAR_REQUIRED_ERROR
        else -> null
      }
    val dateError = if (titleError == null) validateDates(form) else null
    return when {
      titleError != null -> {
        updateForm { it.copy(titleError = titleError) }
        true
      }
      dateError != null -> {
        updateForm { it.copy(dateError = dateError) }
        true
      }
      else -> false
    }
  }

  private fun validateDates(form: EventFormState): String? =
    if (form.isAllDay) {
      val start = form.startDate
      val end = form.endDate
      if (start != null && end != null && end < start) DATE_RANGE_ERROR else null
    } else {
      val start = form.startAt
      val end = form.endAt
      if (start != null && end != null && end < start) TIME_RANGE_ERROR else null
    }

  private suspend fun persistEvent(trimmedTitle: String, form: EventFormState) {
    val now = System.currentTimeMillis()
    val event = buildEvent(trimmedTitle, form, now)
    eventRepository.upsert(event)
    syncReminders(event.eventId, form.reminders, now)
  }

  private suspend fun buildEvent(trimmedTitle: String, form: EventFormState, now: Long): Event {
    return if (isEditing) {
      val existing =
        checkNotNull(eventRepository.getById(eventId!!)) { "Event not found during save" }
      existing.copy(
        title = trimmedTitle,
        description = form.description.trim().ifBlank { null },
        calendarId = requireNotNull(form.calendarId),
        isAllDay = form.isAllDay,
        startDate = form.startDate,
        endDate = form.endDate,
        startAt = form.startAt,
        endAt = form.endAt,
        timezone = form.timezone,
        eventTypeId = form.eventTypeId,
        location = form.location?.trim()?.ifBlank { null },
        recurrenceRule = form.recurrenceRule,
        updatedAt = now,
      )
    } else {
      Event(
        eventId = UUID.randomUUID().toString(),
        calendarId = requireNotNull(form.calendarId),
        spaceId = sessionProvider.spaceId,
        tenantId = sessionProvider.tenantId,
        title = trimmedTitle,
        description = form.description.trim().ifBlank { null },
        isAllDay = form.isAllDay,
        startDate = form.startDate,
        endDate = form.endDate,
        startAt = form.startAt,
        endAt = form.endAt,
        timezone = form.timezone,
        eventTypeId = form.eventTypeId,
        location = form.location?.trim()?.ifBlank { null },
        recurrenceRule = form.recurrenceRule,
        createdAt = now,
        updatedAt = now,
      )
    }
  }

  private suspend fun syncReminders(
    eventId: String,
    desiredReminders: List<ReminderEntry>,
    now: Long,
  ) {
    val existingReminders =
      eventReminderRepository.observeByEvent(eventId).first().filter { it.deletedAt == null }

    val existingById = existingReminders.associateBy { it.reminderId }
    val desiredIds = desiredReminders.map { it.id }.toSet()

    // Delete removed reminders.
    for (existing in existingReminders) {
      if (existing.reminderId !in desiredIds) {
        eventReminderRepository.delete(existing.reminderId)
      }
    }

    // Upsert new reminders.
    for (entry in desiredReminders) {
      if (entry.id !in existingById) {
        eventReminderRepository.upsert(
          EventReminder(
            reminderId = entry.id,
            eventId = eventId,
            minutesBefore = entry.minutesBefore,
            createdAt = now,
            updatedAt = now,
          )
        )
      }
    }
  }

  fun downloadAttachment(item: AttachmentUiItem) {
    viewModelScope.launch {
      val attachment = attachmentRepository.getById(item.attachmentId) ?: return@launch
      attachmentManager.download(attachment)
    }
  }

  fun deleteAttachment(attachmentId: String) {
    viewModelScope.launch { attachmentRepository.delete(attachmentId) }
  }

  private fun Attachment.toUiItem(): AttachmentUiItem =
    AttachmentUiItem(
      attachmentId = attachmentId,
      fileName = fileName,
      mimeType = mimeType,
      fileSize = fileSize,
      downloadState = attachmentManager.observeDownloadState(attachmentId).value,
      remoteUrl = remoteUrl,
      localPath = localPath,
    )

  private fun updateForm(transform: (EventFormState) -> EventFormState) {
    _uiState.update { state ->
      if (state is EventCreateEditUiState.Ready) {
        state.copy(formState = transform(state.formState))
      } else {
        state
      }
    }
  }

  companion object {
    internal const val KEY_EVENT_ID = "eventId"
    internal const val KEY_CALENDAR_ID = "calendarId"
    internal const val KEY_INITIAL_DATE_MILLIS = "initialDateMillis"
    internal const val TITLE_EMPTY_ERROR = "Title cannot be empty"
    internal const val CALENDAR_REQUIRED_ERROR = "A calendar must be selected"
    internal const val DATE_RANGE_ERROR = "End date must be on or after start date"
    internal const val TIME_RANGE_ERROR = "End time must be on or after start time"
    internal const val SAVE_FAILED_ERROR = "Save failed"
    internal const val STOP_TIMEOUT_MILLIS = 5_000L
  }
}
