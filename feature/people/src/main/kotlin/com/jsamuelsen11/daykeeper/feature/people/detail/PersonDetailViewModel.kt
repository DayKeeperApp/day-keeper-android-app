package com.jsamuelsen11.daykeeper.feature.people.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.AddressRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ContactMethodRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ImportantDateRepository
import com.jsamuelsen11.daykeeper.core.data.repository.PersonRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

class PersonDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val personRepository: PersonRepository,
  private val contactMethodRepository: ContactMethodRepository,
  private val addressRepository: AddressRepository,
  private val importantDateRepository: ImportantDateRepository,
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
      ) { person, contacts, addresses, dates ->
        if (person == null) {
          PersonDetailUiState.Error("Person not found")
        } else {
          PersonDetailUiState.Success(
            person = person,
            contactMethods = contacts,
            addresses = addresses,
            importantDates = dates,
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
}
