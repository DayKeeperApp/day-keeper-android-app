package com.jsamuelsen11.daykeeper.feature.calendar.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.attachment.AttachmentManager
import com.jsamuelsen11.daykeeper.core.data.repository.AttachmentRepository
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventReminderRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventTypeRepository
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachableEntityType
import com.jsamuelsen11.daykeeper.core.model.attachment.Attachment
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

/**
 * ViewModel for the event detail screen.
 *
 * Observes a single event by ID (extracted from [SavedStateHandle]) and reactively resolves its
 * associated [com.jsamuelsen11.daykeeper.core.model.calendar.Calendar],
 * [com.jsamuelsen11.daykeeper.core.model.calendar.EventType], and reminders. Exposes [uiState] as a
 * [StateFlow] of [EventDetailUiState].
 *
 * @param savedStateHandle Navigation back-stack handle; must contain an `eventId` key.
 * @param eventRepository Source of truth for event data.
 * @param calendarRepository Source of truth for calendar data.
 * @param eventTypeRepository Source of truth for event type data.
 * @param eventReminderRepository Source of truth for reminder data.
 * @param attachmentRepository Source of truth for attachment data.
 * @param attachmentManager Manages download, upload, and local cache of attachments.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val eventRepository: EventRepository,
  private val calendarRepository: CalendarRepository,
  private val eventTypeRepository: EventTypeRepository,
  private val eventReminderRepository: EventReminderRepository,
  private val attachmentRepository: AttachmentRepository,
  private val attachmentManager: AttachmentManager,
) : ViewModel() {

  private val eventId: String = checkNotNull(savedStateHandle["eventId"])

  /** The reactive UI state for this screen. Starts as [EventDetailUiState.Loading]. */
  val uiState: StateFlow<EventDetailUiState> =
    eventRepository
      .observeById(eventId)
      .flatMapLatest { event ->
        if (event == null || event.deletedAt != null) {
          flowOf(EventDetailUiState.Error("Event not found"))
        } else {
          val calendarFlow = calendarRepository.observeById(event.calendarId)
          val eventTypeFlow =
            if (event.eventTypeId != null) {
              eventTypeRepository.observeAll().flatMapLatest { types ->
                flowOf(types.find { it.eventTypeId == event.eventTypeId })
              }
            } else {
              flowOf(null)
            }
          val remindersFlow =
            eventReminderRepository.observeByEvent(eventId).flatMapLatest { reminders ->
              flowOf(reminders.filter { it.deletedAt == null })
            }
          val attachmentsFlow =
            attachmentRepository.observeByEntity(AttachableEntityType.EVENT, eventId)

          combine(calendarFlow, eventTypeFlow, remindersFlow, attachmentsFlow) {
            calendar,
            eventType,
            reminders,
            attachments ->
            EventDetailUiState.Success(
              event = event,
              calendar = calendar?.takeIf { it.deletedAt == null },
              eventType = eventType,
              reminders = reminders,
              attachments = attachments.map { it.toUiItem() },
            )
          }
        }
      }
      .catch { e -> emit(EventDetailUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        EventDetailUiState.Loading,
      )

  /** Soft-deletes the current event by delegating to [EventRepository.delete]. */
  fun deleteEvent() {
    viewModelScope.launch { eventRepository.delete(eventId) }
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
}
