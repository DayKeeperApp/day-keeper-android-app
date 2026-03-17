package com.jsamuelsen11.daykeeper.feature.tasks.project.detail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import com.jsamuelsen11.daykeeper.feature.tasks.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_PROJECT_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_TASK_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_TASK_ID_2
import com.jsamuelsen11.daykeeper.feature.tasks.makeCategory
import com.jsamuelsen11.daykeeper.feature.tasks.makeProject
import com.jsamuelsen11.daykeeper.feature.tasks.makeTask
import com.jsamuelsen11.daykeeper.feature.tasks.navigation.ProjectDetailRoute
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class ProjectDetailViewModelTest {

  private val projectRepository = mockk<ProjectRepository>()
  private val taskRepository = mockk<TaskRepository>()
  private val categoryRepository = mockk<TaskCategoryRepository>()
  private val savedStateHandle = mockk<SavedStateHandle>()

  @BeforeEach
  fun setUp() {
    mockkStatic("androidx.navigation.SavedStateHandleKt")
    every { savedStateHandle.toRoute<ProjectDetailRoute>() } returns
      ProjectDetailRoute(projectId = TEST_PROJECT_ID)
    every { projectRepository.observeById(TEST_PROJECT_ID) } returns flowOf(makeProject())
    every { taskRepository.observeByProject(TEST_PROJECT_ID) } returns flowOf(emptyList())
    every { categoryRepository.observeAll() } returns flowOf(emptyList())
  }

  @AfterEach
  fun tearDown() {
    unmockkStatic("androidx.navigation.SavedStateHandleKt")
  }

  private fun createViewModel(): ProjectDetailViewModel =
    ProjectDetailViewModel(
      savedStateHandle = savedStateHandle,
      projectRepository = projectRepository,
      taskRepository = taskRepository,
      taskCategoryRepository = categoryRepository,
    )

  @Test
  fun `project found emits Success state`() = runTest {
    val project = makeProject(projectId = TEST_PROJECT_ID, name = "Work")
    every { projectRepository.observeById(TEST_PROJECT_ID) } returns flowOf(project)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectDetailUiState.Success
      state.project.projectId shouldBe TEST_PROJECT_ID
      state.project.name shouldBe "Work"
    }
  }

  @Test
  fun `project not found emits Error state`() = runTest {
    every { projectRepository.observeById(TEST_PROJECT_ID) } returns flowOf(null)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectDetailUiState.Error
      state.message shouldBe "Project not found"
    }
  }

  @Test
  fun `tasks are included in Success state`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, projectId = TEST_PROJECT_ID)
    every { taskRepository.observeByProject(TEST_PROJECT_ID) } returns flowOf(listOf(task))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectDetailUiState.Success
      state.tasks.size shouldBe 1
      state.tasks.first().task.taskId shouldBe TEST_TASK_ID
    }
  }

  @Test
  fun `completedCount and totalCount are calculated correctly`() = runTest {
    val done = makeTask(taskId = TEST_TASK_ID, status = TaskStatus.DONE)
    val todo = makeTask(taskId = TEST_TASK_ID_2, status = TaskStatus.TODO)
    every { taskRepository.observeByProject(TEST_PROJECT_ID) } returns flowOf(listOf(done, todo))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectDetailUiState.Success
      state.totalCount shouldBe 2
      state.completedCount shouldBe 1
    }
  }

  @Test
  fun `tasks enriched with project name and category`() = runTest {
    val category = makeCategory(name = "Personal")
    val task =
      makeTask(taskId = TEST_TASK_ID, projectId = TEST_PROJECT_ID, categoryId = category.categoryId)
    every { taskRepository.observeByProject(TEST_PROJECT_ID) } returns flowOf(listOf(task))
    every { categoryRepository.observeAll() } returns flowOf(listOf(category))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectDetailUiState.Success
      val item = state.tasks.first()
      item.categoryName shouldBe "Personal"
      item.projectName shouldBe makeProject().name
    }
  }

  @Test
  fun `deleted tasks are excluded from the task list`() = runTest {
    val deleted = makeTask(taskId = TEST_TASK_ID).copy(deletedAt = 999L)
    every { taskRepository.observeByProject(TEST_PROJECT_ID) } returns flowOf(listOf(deleted))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as ProjectDetailUiState.Success
      state.tasks shouldBe emptyList()
    }
  }

  @Test
  fun `toggleTaskComplete changes TODO task to DONE`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, status = TaskStatus.TODO)
    coEvery { taskRepository.getById(TEST_TASK_ID) } returns task
    coEvery { taskRepository.upsert(any()) } just runs

    val viewModel = createViewModel()
    viewModel.toggleTaskComplete(TEST_TASK_ID)

    coVerify { taskRepository.upsert(match { it.status == TaskStatus.DONE }) }
  }

  @Test
  fun `toggleTaskComplete changes DONE task back to TODO`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, status = TaskStatus.DONE)
    coEvery { taskRepository.getById(TEST_TASK_ID) } returns task
    coEvery { taskRepository.upsert(any()) } just runs

    val viewModel = createViewModel()
    viewModel.toggleTaskComplete(TEST_TASK_ID)

    coVerify { taskRepository.upsert(match { it.status == TaskStatus.TODO }) }
  }

  @Test
  fun `deleteProject delegates to repository delete`() = runTest {
    coEvery { projectRepository.delete(any()) } just runs

    val viewModel = createViewModel()
    viewModel.deleteProject()

    coVerify { projectRepository.delete(TEST_PROJECT_ID) }
  }

  @Test
  fun `uiState type is ProjectDetailUiState`() = runTest {
    val viewModel = createViewModel()
    viewModel.uiState.value.shouldBeInstanceOf<ProjectDetailUiState>()
  }
}
