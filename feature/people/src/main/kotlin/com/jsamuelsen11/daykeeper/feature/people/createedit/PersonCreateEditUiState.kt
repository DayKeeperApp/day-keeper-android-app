package com.jsamuelsen11.daykeeper.feature.people.createedit

import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethodType
import java.util.UUID

sealed interface PersonCreateEditUiState {
  data object Loading : PersonCreateEditUiState

  data class Ready(
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val notes: String = "",
    val contactMethods: List<ContactMethodFormEntry> = emptyList(),
    val addresses: List<AddressFormEntry> = emptyList(),
    val importantDates: List<ImportantDateFormEntry> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val saveError: String? = null,
    val attachments: List<AttachmentUiItem> = emptyList(),
  ) : PersonCreateEditUiState
}

data class ContactMethodFormEntry(
  val tempId: String = UUID.randomUUID().toString(),
  val existingId: String? = null,
  val originalCreatedAt: Long? = null,
  val type: ContactMethodType = ContactMethodType.PHONE,
  val value: String = "",
  val label: String = "",
  val isPrimary: Boolean = false,
)

data class AddressFormEntry(
  val tempId: String = UUID.randomUUID().toString(),
  val existingId: String? = null,
  val originalCreatedAt: Long? = null,
  val label: String = "",
  val street: String = "",
  val city: String = "",
  val state: String = "",
  val postalCode: String = "",
  val country: String = "",
)

data class ImportantDateFormEntry(
  val tempId: String = UUID.randomUUID().toString(),
  val existingId: String? = null,
  val originalCreatedAt: Long? = null,
  val label: String = "",
  val date: String = "",
)

sealed interface PersonCreateEditEvent {
  data object Saved : PersonCreateEditEvent
}
