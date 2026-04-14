package com.jsamuelsen11.daykeeper.feature.profile.space.createedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.SpaceMemberRepository
import com.jsamuelsen11.daykeeper.core.data.repository.SpaceRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import com.jsamuelsen11.daykeeper.core.model.space.Space
import com.jsamuelsen11.daykeeper.core.model.space.SpaceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpaceCreateEditViewModel(
  savedStateHandle: SavedStateHandle,
  private val spaceRepository: SpaceRepository,
  private val spaceMemberRepository: SpaceMemberRepository,
  private val sessionProvider: CurrentSessionProvider,
) : ViewModel() {

  private val spaceId: String? = savedStateHandle.get<String>("spaceId")
  private val isEditMode = spaceId != null

  private val _uiState = MutableStateFlow<SpaceCreateEditUiState>(SpaceCreateEditUiState.Loading)
  val uiState: StateFlow<SpaceCreateEditUiState> = _uiState.asStateFlow()

  private val _saveComplete = MutableStateFlow(false)
  val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

  init {
    loadSpace()
  }

  private fun loadSpace() {
    viewModelScope.launch {
      if (isEditMode && spaceId != null) {
        val space = spaceRepository.getById(spaceId)
        if (space != null) {
          spaceMemberRepository.observeBySpace(spaceId).collect { members ->
            _uiState.value =
              SpaceCreateEditUiState.Success(
                isEditMode = true,
                name = space.name,
                type = space.type,
                members = members,
              )
          }
        } else {
          _uiState.value = SpaceCreateEditUiState.Error("Space not found")
        }
      } else {
        _uiState.value =
          SpaceCreateEditUiState.Success(
            isEditMode = false,
            name = "",
            type = SpaceType.SHARED,
            members = emptyList(),
          )
      }
    }
  }

  fun updateName(name: String) {
    _uiState.update { state ->
      if (state is SpaceCreateEditUiState.Success) state.copy(name = name) else state
    }
  }

  fun updateType(type: SpaceType) {
    _uiState.update { state ->
      if (state is SpaceCreateEditUiState.Success) state.copy(type = type) else state
    }
  }

  fun save() {
    val state = _uiState.value
    if (state !is SpaceCreateEditUiState.Success || state.name.isBlank()) return

    _uiState.update { if (it is SpaceCreateEditUiState.Success) it.copy(isSaving = true) else it }

    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val space =
        if (isEditMode && spaceId != null) {
          val existing = spaceRepository.getById(spaceId) ?: return@launch
          existing.copy(
            name = state.name,
            normalizedName = state.name.lowercase().trim(),
            type = state.type,
            updatedAt = now,
          )
        } else {
          Space(
            spaceId = java.util.UUID.randomUUID().toString(),
            tenantId = sessionProvider.tenantId,
            name = state.name,
            normalizedName = state.name.lowercase().trim(),
            type = state.type,
            createdAt = now,
            updatedAt = now,
          )
        }
      spaceRepository.upsert(space)
      _saveComplete.value = true
    }
  }

  fun deleteSpace() {
    if (!isEditMode || spaceId == null) return
    viewModelScope.launch {
      spaceRepository.delete(spaceId)
      _saveComplete.value = true
    }
  }
}
