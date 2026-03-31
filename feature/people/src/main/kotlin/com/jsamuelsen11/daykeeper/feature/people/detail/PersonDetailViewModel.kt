package com.jsamuelsen11.daykeeper.feature.people.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.attachment.AttachmentManager
import com.jsamuelsen11.daykeeper.core.data.repository.AddressRepository
import com.jsamuelsen11.daykeeper.core.data.repository.AttachmentRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ContactMethodRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ImportantDateRepository
import com.jsamuelsen11.daykeeper.core.data.repository.PersonRepository
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachableEntityType
import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalCoroutinesApi::class)
class PersonDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val personRepository: PersonRepository,
  private val contactMethodRepository: ContactMethodRepository,
  private val addressRepository: AddressRepository,
  private val importantDateRepository: ImportantDateRepository,
  private val attachmentRepository: AttachmentRepository,
  private val attachmentManager: AttachmentManager,
) : ViewModel() {

  private val personId: String = checkNotNull(savedStateHandle["personId"])

  private val _events = Channel<PersonDetailEvent>(Channel.BUFFERED)
  val events = _events.receiveAsFlow()

  val uiState: StateFlow<PersonDetailUiState> =
    combine(
        personRepository.observeById(personId),
        contactMethodRepository.observeByPerson(personId),
        addressRepository.observeByPerson(personId),
        importantDateRepository.observeByPerson(personId),
        attachmentRepository.observeByEntity(AttachableEntityType.PERSON, personId).flatMapLatest {
          attachments ->
          if (attachments.isEmpty()) {
            flowOf(emptyList())
          } else {
            combine(
              attachments.map { attachment ->
                attachmentManager.observeDownloadState(attachment.attachmentId).map { downloadState
                  ->
                  AttachmentUiItem(
                    attachmentId = attachment.attachmentId,
                    fileName = attachment.fileName,
                    mimeType = attachment.mimeType,
                    fileSize = attachment.fileSize,
                    downloadState = downloadState,
                    remoteUrl = attachment.remoteUrl,
                    localPath = attachment.localPath,
                  )
                }
              }
            ) { items ->
              items.toList()
            }
          }
        },
      ) { person, contacts, addresses, dates, attachments ->
        if (person == null) {
          PersonDetailUiState.Error("Person not found")
        } else {
          PersonDetailUiState.Success(
            person = person,
            contactMethods = contacts,
            addresses = addresses,
            importantDates = dates,
            attachments = attachments,
          )
        }
      }
      .catch { e -> emit(PersonDetailUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        PersonDetailUiState.Loading,
      )

  fun deletePerson() {
    viewModelScope.launch {
      val state = uiState.value as? PersonDetailUiState.Success ?: return@launch
      state.contactMethods.forEach { contactMethodRepository.delete(it.contactMethodId) }
      state.addresses.forEach { addressRepository.delete(it.addressId) }
      state.importantDates.forEach { importantDateRepository.delete(it.importantDateId) }
      personRepository.delete(personId)
      _events.send(PersonDetailEvent.Deleted)
    }
  }

  fun downloadAttachment(item: AttachmentUiItem) {
    viewModelScope.launch {
      val attachment = attachmentRepository.getById(item.attachmentId) ?: return@launch
      attachmentManager.download(attachment)
    }
  }

  fun deleteAttachment(attachmentId: String) {
    viewModelScope.launch {
      attachmentManager.deleteLocal(attachmentId)
      attachmentRepository.delete(attachmentId)
    }
  }
}
