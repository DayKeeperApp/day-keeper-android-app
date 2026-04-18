package com.jsamuelsen11.daykeeper.feature.profile.space

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.SpaceMemberRepository
import com.jsamuelsen11.daykeeper.core.data.repository.SpaceRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import com.jsamuelsen11.daykeeper.core.model.space.SpaceType
import com.jsamuelsen11.daykeeper.feature.profile.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.profile.TEST_SPACE_ID
import com.jsamuelsen11.daykeeper.feature.profile.TEST_SPACE_ID_2
import com.jsamuelsen11.daykeeper.feature.profile.TEST_TENANT_ID
import com.jsamuelsen11.daykeeper.feature.profile.makeSpace
import com.jsamuelsen11.daykeeper.feature.profile.makeSpaceMember
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class SpaceManagementViewModelTest {

  private val spaceRepository = mockk<SpaceRepository>()
  private val spaceMemberRepository = mockk<SpaceMemberRepository>()
  private val sessionProvider = mockk<CurrentSessionProvider>()

  @BeforeEach
  fun setUp() {
    every { sessionProvider.tenantId } returns TEST_TENANT_ID
    every { spaceRepository.observeByTenant(TEST_TENANT_ID) } returns flowOf(emptyList())
  }

  private fun createViewModel(): SpaceManagementViewModel =
    SpaceManagementViewModel(
      spaceRepository = spaceRepository,
      spaceMemberRepository = spaceMemberRepository,
      sessionProvider = sessionProvider,
    )

  @Test
  fun `empty spaces emits Success with empty map`() = runTest {
    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<SpaceManagementUiState.Success>()
      state.groupedSpaces shouldBe emptyMap()
    }
  }

  @Test
  fun `spaces grouped by type with member counts`() = runTest {
    val personalSpace = makeSpace(spaceId = TEST_SPACE_ID, type = SpaceType.PERSONAL)
    val sharedSpace = makeSpace(spaceId = TEST_SPACE_ID_2, name = "Family", type = SpaceType.SHARED)

    every { spaceRepository.observeByTenant(TEST_TENANT_ID) } returns
      flowOf(listOf(personalSpace, sharedSpace))
    every { spaceMemberRepository.observeBySpace(TEST_SPACE_ID) } returns
      flowOf(listOf(makeSpaceMember(spaceId = TEST_SPACE_ID)))
    every { spaceMemberRepository.observeBySpace(TEST_SPACE_ID_2) } returns
      flowOf(
        listOf(
          makeSpaceMember(spaceId = TEST_SPACE_ID_2),
          makeSpaceMember(spaceId = TEST_SPACE_ID_2, tenantId = "other-tenant"),
        )
      )

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<SpaceManagementUiState.Success>()
      state.groupedSpaces[SpaceType.PERSONAL]!!.size shouldBe 1
      state.groupedSpaces[SpaceType.PERSONAL]!![0].memberCount shouldBe 1
      state.groupedSpaces[SpaceType.SHARED]!!.size shouldBe 1
      state.groupedSpaces[SpaceType.SHARED]!![0].memberCount shouldBe 2
    }
  }

  @Test
  fun `deleteSpace calls repository`() = runTest {
    coEvery { spaceRepository.delete(TEST_SPACE_ID) } just runs

    val viewModel = createViewModel()
    viewModel.deleteSpace(TEST_SPACE_ID)

    coVerify { spaceRepository.delete(TEST_SPACE_ID) }
  }
}
