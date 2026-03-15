package com.jsamuelsen11.daykeeper.feature.people.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.ContactMethodRepository
import com.jsamuelsen11.daykeeper.core.data.repository.PersonRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val DEFAULT_SPACE_ID = "default-space"

class PeopleListViewModel(
  private val personRepository: PersonRepository,
  private val contactMethodRepository: ContactMethodRepository,
) : ViewModel() {

  private val searchQuery = MutableStateFlow("")
  private val sortOrder = MutableStateFlow(PeopleSortOrder.FIRST_NAME_ASC)

  @OptIn(ExperimentalCoroutinesApi::class)
  val uiState: StateFlow<PeopleListUiState> =
    combine(personRepository.observeBySpace(DEFAULT_SPACE_ID), searchQuery, sortOrder) {
        people,
        query,
        sort ->
        Triple(people, query, sort)
      }
      .flatMapLatest { (people, query, sort) ->
        val filtered =
          if (query.isBlank()) people
          else
            people.filter { person ->
              person.firstName.contains(query, ignoreCase = true) ||
                person.lastName.contains(query, ignoreCase = true) ||
                person.nickname?.contains(query, ignoreCase = true) == true
            }

        val sorted =
          when (sort) {
            PeopleSortOrder.FIRST_NAME_ASC ->
              filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.firstName })
            PeopleSortOrder.LAST_NAME_ASC ->
              filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.lastName })
          }

        if (sorted.isEmpty()) {
          flowOf(PeopleListUiState.Success(emptyList(), query, sort))
        } else {
          val cmFlows = sorted.map { contactMethodRepository.observeByPerson(it.personId) }
          combine(cmFlows) { cmArrays ->
            val summaries =
              sorted.mapIndexed { index, person ->
                PersonSummary(
                  person = person,
                  primaryContactMethod = cmArrays[index].firstOrNull { it.isPrimary },
                )
              }
            PeopleListUiState.Success(summaries, query, sort) as PeopleListUiState
          }
        }
      }
      .catch { e -> emit(PeopleListUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        PeopleListUiState.Loading,
      )

  fun onSearchQueryChanged(query: String) {
    searchQuery.value = query
  }

  fun onSortOrderChanged(order: PeopleSortOrder) {
    sortOrder.value = order
  }

  fun deletePerson(personId: String) {
    viewModelScope.launch { personRepository.delete(personId) }
  }
}
