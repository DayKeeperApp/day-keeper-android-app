package com.jsamuelsen11.daykeeper.feature.profile.space

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsamuelsen11.daykeeper.core.data.repository.SpaceMemberRepository
import com.jsamuelsen11.daykeeper.core.data.repository.SpaceRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
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

class SpaceManagementViewModel(
  private val spaceRepository: SpaceRepository,
  private val spaceMemberRepository: SpaceMemberRepository,
  private val sessionProvider: CurrentSessionProvider,
) : ViewModel() {

  @OptIn(ExperimentalCoroutinesApi::class)
  val uiState: StateFlow<SpaceManagementUiState> =
    spaceRepository
      .observeByTenant(sessionProvider.tenantId)
      .flatMapLatest { spaces ->
        if (spaces.isEmpty()) {
          flowOf(SpaceManagementUiState.Success(emptyMap()))
        } else {
          val memberFlows =
            spaces.map { space -> spaceMemberRepository.observeBySpace(space.spaceId) }
          combine(memberFlows) { memberArrays ->
            val spacesWithMeta =
              spaces.mapIndexed { index, space ->
                val members = memberArrays[index]
                SpaceWithMeta(
                  space = space,
                  memberCount = members.size,
                  userRole = members.find { it.tenantId == sessionProvider.tenantId }?.role,
                )
              }
            val grouped = spacesWithMeta.groupBy { it.space.type }
            SpaceManagementUiState.Success(grouped) as SpaceManagementUiState
          }
        }
      }
      .catch { e -> emit(SpaceManagementUiState.Error(e.message ?: "Unknown error")) }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        SpaceManagementUiState.Loading,
      )

  fun deleteSpace(spaceId: String) {
    viewModelScope.launch { spaceRepository.delete(spaceId) }
  }
}
