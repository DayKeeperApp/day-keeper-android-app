package com.jsamuelsen11.daykeeper.feature.people.createedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.AddressRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ContactMethodRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ImportantDateRepository
import com.jsamuelsen11.daykeeper.core.data.repository.PersonRepository
import com.jsamuelsen11.daykeeper.core.model.people.Address
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethod
import com.jsamuelsen11.daykeeper.core.model.people.ImportantDate
import com.jsamuelsen11.daykeeper.core.model.people.Person
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PersonCreateEditViewModel(
  savedStateHandle: SavedStateHandle,
  private val personRepository: PersonRepository,
  private val contactMethodRepository: ContactMethodRepository,
  private val addressRepository: AddressRepository,
  private val importantDateRepository: ImportantDateRepository,
) : ViewModel() {

  private val personId: String? = savedStateHandle["personId"]
  private val isEditing = personId != null

  private val _uiState = MutableStateFlow<PersonCreateEditUiState>(PersonCreateEditUiState.Loading)
  val uiState: StateFlow<PersonCreateEditUiState> = _uiState.asStateFlow()

  private val _events = Channel<PersonCreateEditEvent>(Channel.BUFFERED)
  val events = _events.receiveAsFlow()

  private var originalContactMethodIds = emptySet<String>()
  private var originalAddressIds = emptySet<String>()
  private var originalImportantDateIds = emptySet<String>()

  init {
    viewModelScope.launch { loadInitialState() }
  }

  fun onFirstNameChanged(value: String) {
    updateReady { it.copy(firstName = value, firstNameError = null) }
  }

  fun onLastNameChanged(value: String) {
    updateReady { it.copy(lastName = value, lastNameError = null) }
  }

  fun onNicknameChanged(value: String) {
    updateReady { it.copy(nickname = value) }
  }

  fun onNotesChanged(value: String) {
    updateReady { it.copy(notes = value) }
  }

  fun addContactMethod() {
    updateReady { it.copy(contactMethods = it.contactMethods + ContactMethodFormEntry()) }
  }

  fun removeContactMethod(tempId: String) {
    updateReady { it.copy(contactMethods = it.contactMethods.filter { cm -> cm.tempId != tempId }) }
  }

  fun updateContactMethod(tempId: String, entry: ContactMethodFormEntry) {
    updateReady {
      it.copy(
        contactMethods = it.contactMethods.map { cm -> if (cm.tempId == tempId) entry else cm }
      )
    }
  }

  fun addAddress() {
    updateReady { it.copy(addresses = it.addresses + AddressFormEntry()) }
  }

  fun removeAddress(tempId: String) {
    updateReady { it.copy(addresses = it.addresses.filter { a -> a.tempId != tempId }) }
  }

  fun updateAddress(tempId: String, entry: AddressFormEntry) {
    updateReady {
      it.copy(addresses = it.addresses.map { a -> if (a.tempId == tempId) entry else a })
    }
  }

  fun addImportantDate() {
    updateReady { it.copy(importantDates = it.importantDates + ImportantDateFormEntry()) }
  }

  fun removeImportantDate(tempId: String) {
    updateReady { it.copy(importantDates = it.importantDates.filter { d -> d.tempId != tempId }) }
  }

  fun updateImportantDate(tempId: String, entry: ImportantDateFormEntry) {
    updateReady {
      it.copy(importantDates = it.importantDates.map { d -> if (d.tempId == tempId) entry else d })
    }
  }

  fun onSave() {
    val state = _uiState.value as? PersonCreateEditUiState.Ready ?: return
    val validationError = validate(state)
    if (validationError != null) {
      updateReady { validationError }
      return
    }
    updateReady { it.copy(isSaving = true) }
    viewModelScope.launch { performSave(state) }
  }

  private suspend fun loadInitialState() {
    if (isEditing) {
      val person = personRepository.getById(personId!!)
      val contacts = contactMethodRepository.observeByPerson(personId).first()
      val addresses = addressRepository.observeByPerson(personId).first()
      val dates = importantDateRepository.observeByPerson(personId).first()

      originalContactMethodIds = contacts.map { it.contactMethodId }.toSet()
      originalAddressIds = addresses.map { it.addressId }.toSet()
      originalImportantDateIds = dates.map { it.importantDateId }.toSet()

      _uiState.value = buildEditState(person, contacts, addresses, dates)
    } else {
      _uiState.value = PersonCreateEditUiState.Ready()
    }
  }

  private fun buildEditState(
    person: Person?,
    contacts: List<ContactMethod>,
    addresses: List<Address>,
    dates: List<ImportantDate>,
  ): PersonCreateEditUiState.Ready =
    PersonCreateEditUiState.Ready(
      firstName = person?.firstName.orEmpty(),
      lastName = person?.lastName.orEmpty(),
      nickname = person?.nickname.orEmpty(),
      notes = person?.notes.orEmpty(),
      contactMethods =
        contacts.map {
          ContactMethodFormEntry(
            existingId = it.contactMethodId,
            originalCreatedAt = it.createdAt,
            type = it.type,
            value = it.value,
            label = it.label,
            isPrimary = it.isPrimary,
          )
        },
      addresses =
        addresses.map {
          AddressFormEntry(
            existingId = it.addressId,
            originalCreatedAt = it.createdAt,
            label = it.label,
            street = it.street.orEmpty(),
            city = it.city.orEmpty(),
            state = it.state.orEmpty(),
            postalCode = it.postalCode.orEmpty(),
            country = it.country.orEmpty(),
          )
        },
      importantDates =
        dates.map {
          ImportantDateFormEntry(
            existingId = it.importantDateId,
            originalCreatedAt = it.createdAt,
            label = it.label,
            date = it.date,
          )
        },
      isEditing = true,
    )

  private fun validate(state: PersonCreateEditUiState.Ready): PersonCreateEditUiState.Ready? =
    when {
      state.firstName.isBlank() -> state.copy(firstNameError = "First name is required")
      state.lastName.isBlank() -> state.copy(lastNameError = "Last name is required")
      else -> null
    }

  private suspend fun performSave(state: PersonCreateEditUiState.Ready) {
    runCatching {
        val now = System.currentTimeMillis()
        val pid = savePerson(state, now)
        saveContactMethods(pid, state.contactMethods, now)
        saveAddresses(pid, state.addresses, now)
        saveImportantDates(pid, state.importantDates, now)
      }
      .onSuccess { _events.send(PersonCreateEditEvent.Saved) }
      .onFailure { error ->
        updateReady { it.copy(isSaving = false, saveError = error.message ?: "Save failed") }
      }
  }

  private suspend fun savePerson(state: PersonCreateEditUiState.Ready, now: Long): String {
    val trimmedFirst = state.firstName.trim()
    val trimmedLast = state.lastName.trim()
    val nick = state.nickname.trim().ifBlank { null }
    val notes = state.notes.trim().ifBlank { null }

    return if (isEditing) {
      val existing = personRepository.getById(personId!!) ?: error("Person not found")
      personRepository.upsert(
        existing.copy(
          firstName = trimmedFirst,
          lastName = trimmedLast,
          nickname = nick,
          notes = notes,
          updatedAt = now,
        )
      )
      personId
    } else {
      val newId = UUID.randomUUID().toString()
      personRepository.upsert(
        Person(
          personId = newId,
          spaceId = DEFAULT_SPACE_ID,
          tenantId = DEFAULT_TENANT_ID,
          firstName = trimmedFirst,
          lastName = trimmedLast,
          nickname = nick,
          notes = notes,
          createdAt = now,
          updatedAt = now,
        )
      )
      newId
    }
  }

  private suspend fun saveContactMethods(
    pid: String,
    entries: List<ContactMethodFormEntry>,
    now: Long,
  ) {
    val currentIds = entries.mapNotNull { it.existingId }.toSet()
    (originalContactMethodIds - currentIds).forEach { contactMethodRepository.delete(it) }
    entries.forEach { entry ->
      contactMethodRepository.upsert(
        ContactMethod(
          contactMethodId = entry.existingId ?: UUID.randomUUID().toString(),
          personId = pid,
          type = entry.type,
          value = entry.value.trim(),
          label = entry.label.trim(),
          isPrimary = entry.isPrimary,
          createdAt = entry.originalCreatedAt ?: now,
          updatedAt = now,
        )
      )
    }
  }

  private suspend fun saveAddresses(pid: String, entries: List<AddressFormEntry>, now: Long) {
    val currentIds = entries.mapNotNull { it.existingId }.toSet()
    (originalAddressIds - currentIds).forEach { addressRepository.delete(it) }
    entries.forEach { entry ->
      addressRepository.upsert(
        Address(
          addressId = entry.existingId ?: UUID.randomUUID().toString(),
          personId = pid,
          label = entry.label.trim(),
          street = entry.street.trim().ifBlank { null },
          city = entry.city.trim().ifBlank { null },
          state = entry.state.trim().ifBlank { null },
          postalCode = entry.postalCode.trim().ifBlank { null },
          country = entry.country.trim().ifBlank { null },
          createdAt = entry.originalCreatedAt ?: now,
          updatedAt = now,
        )
      )
    }
  }

  private suspend fun saveImportantDates(
    pid: String,
    entries: List<ImportantDateFormEntry>,
    now: Long,
  ) {
    val currentIds = entries.mapNotNull { it.existingId }.toSet()
    (originalImportantDateIds - currentIds).forEach { importantDateRepository.delete(it) }
    entries.forEach { entry ->
      importantDateRepository.upsert(
        ImportantDate(
          importantDateId = entry.existingId ?: UUID.randomUUID().toString(),
          personId = pid,
          label = entry.label.trim(),
          date = entry.date.trim(),
          createdAt = entry.originalCreatedAt ?: now,
          updatedAt = now,
        )
      )
    }
  }

  private fun updateReady(
    transform: (PersonCreateEditUiState.Ready) -> PersonCreateEditUiState.Ready
  ) {
    _uiState.update { state ->
      if (state is PersonCreateEditUiState.Ready) transform(state) else state
    }
  }

  companion object {
    private const val DEFAULT_SPACE_ID = "default-space"
    private const val DEFAULT_TENANT_ID = "default-tenant"
  }
}
