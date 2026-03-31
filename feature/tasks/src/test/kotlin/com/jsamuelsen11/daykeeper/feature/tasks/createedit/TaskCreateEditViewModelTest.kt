package com.jsamuelsen11.daykeeper.feature.tasks.createedit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.attachment.AttachmentManager
import com.jsamuelsen11.daykeeper.core.data.repository.AttachmentRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.feature.tasks.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_PROJECT_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_SPACE_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_TASK_ID
import com.jsamuelsen11.daykeeper.feature.tasks.makeCategory
import com.jsamuelsen11.daykeeper.feature.tasks.makeProject
import com.jsamuelsen11.daykeeper.feature.tasks.makeTask
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class TaskCreateEditViewModelTest {

  private val taskRepository = mockk<TaskRepository>()
  private val projectRepository = mockk<ProjectRepository>()
  private val categoryRepository = mockk<TaskCategoryRepository>()
  private val attachmentRepository: AttachmentRepository = mockk(relaxed = true)
  private val attachmentManager: AttachmentManager = mockk(relaxed = true)

  @BeforeEach
  fun setUp() {
    every { projectRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(emptyList())
    every { categoryRepository.observeAll() } returns flowOf(emptyList())
    every { attachmentRepository.observeByEntity(any(), any()) } returns flowOf(emptyList())
  }

  private fun createModeViewModel(): TaskCreateEditViewModel =
    TaskCreateEditViewModel(
      savedStateHandle = SavedStateHandle(),
      taskRepository = taskRepository,
      projectRepository = projectRepository,
      taskCategoryRepository = categoryRepository,
      attachmentRepository = attachmentRepository,
      attachmentManager = attachmentManager,
    )

  private fun editModeViewModel(taskId: String = TEST_TASK_ID): TaskCreateEditViewModel =
    TaskCreateEditViewModel(
      savedStateHandle = SavedStateHandle(mapOf("taskId" to taskId)),
      taskRepository = taskRepository,
      projectRepository = projectRepository,
      taskCategoryRepository = categoryRepository,
      attachmentRepository = attachmentRepository,
      attachmentManager = attachmentManager,
    )

  private fun createModeWithProject(projectId: String = TEST_PROJECT_ID): TaskCreateEditViewModel =
    TaskCreateEditViewModel(
      savedStateHandle = SavedStateHandle(mapOf("projectId" to projectId)),
      taskRepository = taskRepository,
      projectRepository = projectRepository,
      taskCategoryRepository = categoryRepository,
      attachmentRepository = attachmentRepository,
      attachmentManager = attachmentManager,
    )

  // --- Create mode ---

  @Test
  fun `create mode transitions to Ready state`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test { awaitItem().shouldBeInstanceOf<TaskCreateEditUiState.Ready>() }
  }

  @Test
  fun `create mode starts with empty title and isEditing false`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskCreateEditUiState.Ready
      state.title shouldBe ""
      state.isEditing shouldBe false
    }
  }

  @Test
  fun `create mode starts with NONE priority`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskCreateEditUiState.Ready
      state.priority shouldBe Priority.NONE
    }
  }

  @Test
  fun `create mode with projectId pre-selects that project`() = runTest {
    val viewModel = createModeWithProject(projectId = TEST_PROJECT_ID)

    viewModel.uiState.test {
      val state = awaitItem() as TaskCreateEditUiState.Ready
      state.projectId shouldBe TEST_PROJECT_ID
    }
  }

  @Test
  fun `create mode populates available projects`() = runTest {
    val project = makeProject(projectId = TEST_PROJECT_ID, name = "Work")
    every { projectRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(project))

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskCreateEditUiState.Ready
      state.projects.size shouldBe 1
      state.projects.first().projectId shouldBe TEST_PROJECT_ID
    }
  }

  @Test
  fun `create mode populates available categories`() = runTest {
    val category = makeCategory()
    every { categoryRepository.observeAll() } returns flowOf(listOf(category))

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskCreateEditUiState.Ready
      state.categories.size shouldBe 1
    }
  }

  // --- Edit mode ---

  @Test
  fun `edit mode loads existing task title and isEditing true`() = runTest {
    coEvery { taskRepository.getById(TEST_TASK_ID) } returns makeTask(title = "Existing Task")

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskCreateEditUiState.Ready
      state.title shouldBe "Existing Task"
      state.isEditing shouldBe true
    }
  }

  @Test
  fun `edit mode with missing task starts with empty title`() = runTest {
    coEvery { taskRepository.getById(TEST_TASK_ID) } returns null

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskCreateEditUiState.Ready
      state.title shouldBe ""
    }
  }

  // --- onTitleChanged ---

  @Test
  fun `onTitleChanged updates title and clears titleError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem() // initial Ready

      viewModel.onTitleChanged("New Title")

      val state = awaitItem() as TaskCreateEditUiState.Ready
      state.title shouldBe "New Title"
      state.titleError shouldBe null
    }
  }

  @Test
  fun `onTitleChanged clears existing titleError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave() // trigger validation error
      val errorState = awaitItem() as TaskCreateEditUiState.Ready
      errorState.titleError shouldNotBe null

      viewModel.onTitleChanged("Fixed Title")
      val clearedState = awaitItem() as TaskCreateEditUiState.Ready
      clearedState.titleError shouldBe null
    }
  }

  // --- onPrioritySelected ---

  @Test
  fun `onPrioritySelected updates priority`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onPrioritySelected(Priority.HIGH)
      val state = awaitItem() as TaskCreateEditUiState.Ready
      state.priority shouldBe Priority.HIGH
    }
  }

  // --- onSave validation ---

  @Test
  fun `onSave with blank title sets titleError`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave()
      val state = awaitItem() as TaskCreateEditUiState.Ready
      state.titleError shouldBe TaskCreateEditViewModel.TITLE_EMPTY_ERROR
    }
  }

  @Test
  fun `onSave with blank title does not call repository upsert`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onSave()
      awaitItem()
    }

    coVerify(exactly = 0) { taskRepository.upsert(any()) }
  }

  // --- onSave success (create) ---

  @Test
  fun `onSave calls upsert and emits Saved event in create mode`() = runTest {
    coEvery { taskRepository.upsert(any()) } just runs

    val viewModel = createModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem()
        viewModel.onTitleChanged("Buy milk")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe TaskCreateEditEvent.Saved
    }

    coVerify { taskRepository.upsert(any()) }
  }

  @Test
  fun `onSave in create mode upserts task with trimmed title`() = runTest {
    val upsertSlot = slot<com.jsamuelsen11.daykeeper.core.model.task.Task>()
    coEvery { taskRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onTitleChanged("  Trimmed Title  ")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    upsertSlot.captured.title shouldBe "Trimmed Title"
  }

  // --- onSave success (edit) ---

  @Test
  fun `onSave in edit mode calls upsert and emits Saved event`() = runTest {
    coEvery { taskRepository.getById(TEST_TASK_ID) } returns makeTask(title = "Old Title")
    coEvery { taskRepository.upsert(any()) } just runs

    val viewModel = editModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem()
        viewModel.onTitleChanged("New Title")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe TaskCreateEditEvent.Saved
    }

    coVerify { taskRepository.upsert(any()) }
  }

  @Test
  fun `onSave in edit mode preserves taskId`() = runTest {
    coEvery { taskRepository.getById(TEST_TASK_ID) } returns makeTask(taskId = TEST_TASK_ID)

    val upsertSlot = slot<com.jsamuelsen11.daykeeper.core.model.task.Task>()
    coEvery { taskRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onTitleChanged("Updated")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    upsertSlot.captured.taskId shouldBe TEST_TASK_ID
  }

  @Test
  fun `onSave resets isSaving and sets titleError on repository failure`() = runTest {
    coEvery { taskRepository.upsert(any()) } throws RuntimeException("DB error")

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onTitleChanged("Valid Title")
      awaitItem()
      viewModel.onSave()

      val savingState = awaitItem() as TaskCreateEditUiState.Ready
      if (savingState.isSaving) {
        val resetState = awaitItem() as TaskCreateEditUiState.Ready
        resetState.isSaving shouldBe false
        resetState.titleError shouldBe "DB error"
      } else {
        savingState.isSaving shouldBe false
        savingState.titleError shouldBe "DB error"
      }
    }
  }
}
