package com.jsamuelsen11.daykeeper.feature.people.list

import com.jsamuelsen11.daykeeper.core.model.people.ContactMethod
import com.jsamuelsen11.daykeeper.core.model.people.Person

sealed interface PeopleListUiState {
  data object Loading : PeopleListUiState

  data class Success(
    val people: List<PersonSummary>,
    val searchQuery: String = "",
    val sortOrder: PeopleSortOrder = PeopleSortOrder.FIRST_NAME_ASC,
    val isRefreshing: Boolean = false,
  ) : PeopleListUiState

  data class Error(val message: String) : PeopleListUiState
}

data class PersonSummary(val person: Person, val primaryContactMethod: ContactMethod? = null)

enum class PeopleSortOrder {
  FIRST_NAME_ASC,
  LAST_NAME_ASC,
}
