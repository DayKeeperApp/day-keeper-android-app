package com.jsamuelsen11.daykeeper.feature.lists.createedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import com.jsamuelsen11.daykeeper.core.model.list.ShoppingList
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListCreateEditViewModel(
  savedStateHandle: SavedStateHandle,
  private val listRepository: ShoppingListRepository,
  private val sessionProvider: CurrentSessionProvider,
) : ViewModel() {

  private val listId: String? = savedStateHandle["listId"]
  private val isEditing = listId != null

  private val _uiState = MutableStateFlow<ListCreateEditUiState>(ListCreateEditUiState.Loading)
  val uiState: StateFlow<ListCreateEditUiState> = _uiState.asStateFlow()

  private val _events = Channel<ListCreateEditEvent>(Channel.BUFFERED)
  val events = _events.receiveAsFlow()

  init {
    viewModelScope.launch {
      if (isEditing) {
        val list = listRepository.getById(listId!!)
        _uiState.value = ListCreateEditUiState.Ready(name = list?.name.orEmpty(), isEditing = true)
      } else {
        _uiState.value = ListCreateEditUiState.Ready()
      }
    }
  }

  fun onNameChanged(name: String) {
    _uiState.update { state ->
      if (state is ListCreateEditUiState.Ready) state.copy(name = name, nameError = null) else state
    }
  }

  fun onSave() {
    val state = _uiState.value as? ListCreateEditUiState.Ready ?: return
    val trimmedName = state.name.trim()
    if (trimmedName.isBlank()) {
      _uiState.update {
        if (it is ListCreateEditUiState.Ready) it.copy(nameError = "Name cannot be empty") else it
      }
      return
    }
    _uiState.update { if (it is ListCreateEditUiState.Ready) it.copy(isSaving = true) else it }
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val list =
        if (isEditing) {
          val existing = listRepository.getById(listId!!)
          existing?.copy(
            name = trimmedName,
            normalizedName = trimmedName.lowercase().trim(),
            updatedAt = now,
          )
            ?: run {
              resetSaving()
              return@launch
            }
        } else {
          ShoppingList(
            listId = UUID.randomUUID().toString(),
            spaceId = sessionProvider.spaceId,
            tenantId = sessionProvider.tenantId,
            name = trimmedName,
            normalizedName = trimmedName.lowercase().trim(),
            createdAt = now,
            updatedAt = now,
          )
        }
      runCatching { listRepository.upsert(list) }
        .onSuccess { _events.send(ListCreateEditEvent.Saved) }
        .onFailure { error ->
          _uiState.update {
            if (it is ListCreateEditUiState.Ready) {
              it.copy(isSaving = false, nameError = error.message ?: "Save failed")
            } else {
              it
            }
          }
        }
    }
  }

  private fun resetSaving() {
    _uiState.update { if (it is ListCreateEditUiState.Ready) it.copy(isSaving = false) else it }
  }
}

sealed interface ListCreateEditEvent {
  data object Saved : ListCreateEditEvent
}
