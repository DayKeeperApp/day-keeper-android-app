package com.jsamuelsen11.daykeeper.feature.people.detail

import com.jsamuelsen11.daykeeper.core.model.attachment.AttachmentUiItem
import com.jsamuelsen11.daykeeper.core.model.people.Address
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethod
import com.jsamuelsen11.daykeeper.core.model.people.ImportantDate
import com.jsamuelsen11.daykeeper.core.model.people.Person

sealed interface PersonDetailUiState {
  data object Loading : PersonDetailUiState

  data class Success(
    val person: Person,
    val contactMethods: List<ContactMethod>,
    val addresses: List<Address>,
    val importantDates: List<ImportantDate>,
    val attachments: List<AttachmentUiItem> = emptyList(),
  ) : PersonDetailUiState

  data class Error(val message: String) : PersonDetailUiState
}

sealed interface PersonDetailEvent {
  data object Deleted : PersonDetailEvent
}
