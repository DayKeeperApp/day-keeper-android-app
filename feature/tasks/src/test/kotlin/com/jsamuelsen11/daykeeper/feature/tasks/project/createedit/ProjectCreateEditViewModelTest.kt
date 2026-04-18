package com.jsamuelsen11.daykeeper.feature.tasks.project.createedit

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import com.jsamuelsen11.daykeeper.feature.tasks.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_PROJECT_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_SPACE_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_TENANT_ID
import com.jsamuelsen11.daykeeper.feature.tasks.makeProject
import com.jsamuelsen11.daykeeper.feature.tasks.navigation.ProjectCreateEditRoute
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class ProjectCreateEditViewModelTest {

  private val projectRepository = mockk<ProjectRepository>()
  private val savedStateHandle = mockk<SavedStateHandle>()
  private val sessionProvider: CurrentSessionProvider = mockk()

  @BeforeEach
  fun setUp() {
    mockkStatic("androidx.navigation.SavedStateHandleKt")
    every { sessionProvider.spaceId } returns TEST_SPACE_ID
    every { sessionProvider.tenantId } returns TEST_TENANT_ID
  }

  @AfterEach
  fun tearDown() {
    unmockkStatic("androidx.navigation.SavedStateHandleKt")
  }

  private fun createModeViewModel(): ProjectCreateEditViewModel {
    every { savedStateHandle.toRoute<ProjectCreateEditRoute>() } returns
      ProjectCreateEditRoute(projectId = null)
    return ProjectCreateEditViewModel(
      savedStateHandle = savedStateHandle,
      projectRepository = projectRepository,
      sessionProvider = sessionProvider,
    )
  }

  private fun editModeViewModel(projectId: String = TEST_PROJECT_ID): ProjectCreateEditViewModel {
    every { savedStateHandle.toRoute<ProjectCreateEditRoute>() } returns
      ProjectCreateEditRoute(projectId = projectId)
    return ProjectCreateEditViewModel(
      savedStateHandle = savedStateHandle,
      projectRepository = projectRepository,
      sessionProvider = sessionProvider,
    )
  }

  // --- Create mode ---

  @Test
  fun `create mode transitions to Ready state`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test { awaitItem().shouldBeInstanceOf<ProjectCreateEditUiState.Ready>() }
  }

  @Test
  fun `create mode starts with empty name and isEditing false`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectCreateEditUiState.Ready
      state.name shouldBe ""
      state.isEditing shouldBe false
    }
  }

  @Test
  fun `create mode starts with no nameError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectCreateEditUiState.Ready
      state.nameError shouldBe null
    }
  }

  // --- Edit mode ---

  @Test
  fun `edit mode loads existing project name`() = runTest {
    coEvery { projectRepository.getById(TEST_PROJECT_ID) } returns makeProject(name = "My Project")

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectCreateEditUiState.Ready
      state.name shouldBe "My Project"
    }
  }

  @Test
  fun `edit mode has isEditing true`() = runTest {
    coEvery { projectRepository.getById(TEST_PROJECT_ID) } returns makeProject()

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectCreateEditUiState.Ready
      state.isEditing shouldBe true
    }
  }

  @Test
  fun `edit mode with missing project starts with empty name`() = runTest {
    coEvery { projectRepository.getById(TEST_PROJECT_ID) } returns null

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectCreateEditUiState.Ready
      state.name shouldBe ""
      state.isEditing shouldBe true
    }
  }

  // --- onNameChanged ---

  @Test
  fun `onNameChanged updates name and clears nameError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave() // trigger a validation error
      val errorState = awaitItem() as ProjectCreateEditUiState.Ready
      errorState.nameError shouldBe "Name is required"

      viewModel.onNameChanged("Side Project")
      val updatedState = awaitItem() as ProjectCreateEditUiState.Ready
      updatedState.name shouldBe "Side Project"
      updatedState.nameError shouldBe null
    }
  }

  // --- onDescriptionChanged ---

  @Test
  fun `onDescriptionChanged updates description`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onDescriptionChanged("A longer description")
      val state = awaitItem() as ProjectCreateEditUiState.Ready
      state.description shouldBe "A longer description"
    }
  }

  // --- onSave validation ---

  @Test
  fun `onSave with blank name sets nameError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave()
      val state = awaitItem() as ProjectCreateEditUiState.Ready
      state.nameError shouldBe "Name is required"
    }
  }

  @Test
  fun `onSave with blank name does not call repository upsert`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave()
      awaitItem()
    }

    coVerify(exactly = 0) { projectRepository.upsert(any()) }
  }

  // --- onSave success (create) ---

  @Test
  fun `onSave calls upsert and emits Saved event in create mode`() = runTest {
    coEvery { projectRepository.upsert(any()) } just runs

    val viewModel = createModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem()
        viewModel.onNameChanged("New Project")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe ProjectCreateEditEvent.Saved
    }

    coVerify { projectRepository.upsert(any()) }
  }

  @Test
  fun `onSave in create mode upserts project with trimmed name`() = runTest {
    val upsertSlot = slot<com.jsamuelsen11.daykeeper.core.model.task.Project>()
    coEvery { projectRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onNameChanged("  Trimmed  ")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    upsertSlot.captured.name shouldBe "Trimmed"
    upsertSlot.captured.normalizedName shouldBe "trimmed"
  }

  // --- onSave success (edit) ---

  @Test
  fun `onSave in edit mode calls upsert and emits Saved event`() = runTest {
    coEvery { projectRepository.getById(TEST_PROJECT_ID) } returns makeProject(name = "Old Name")
    coEvery { projectRepository.upsert(any()) } just runs

    val viewModel = editModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem()
        viewModel.onNameChanged("New Name")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe ProjectCreateEditEvent.Saved
    }

    coVerify { projectRepository.upsert(any()) }
  }

  @Test
  fun `onSave in edit mode upserts with updated name and preserves projectId`() = runTest {
    coEvery { projectRepository.getById(TEST_PROJECT_ID) } returns makeProject(name = "Old")

    val upsertSlot = slot<com.jsamuelsen11.daykeeper.core.model.task.Project>()
    coEvery { projectRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onNameChanged("Updated Name")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    upsertSlot.captured.projectId shouldBe TEST_PROJECT_ID
    upsertSlot.captured.name shouldBe "Updated Name"
  }
}
