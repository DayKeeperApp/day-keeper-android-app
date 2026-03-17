package com.jsamuelsen11.daykeeper.feature.tasks.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import com.jsamuelsen11.daykeeper.feature.tasks.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_CATEGORY_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_PROJECT_ID
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class TaskDetailViewModelTest {

  private val taskRepository = mockk<TaskRepository>()
  private val projectRepository = mockk<ProjectRepository>()
  private val categoryRepository = mockk<TaskCategoryRepository>()

  private val savedStateHandle = SavedStateHandle(mapOf("taskId" to TEST_TASK_ID))

  @BeforeEach
  fun setUp() {
    every { taskRepository.observeById(TEST_TASK_ID) } returns flowOf(makeTask())
    every { categoryRepository.observeAll() } returns flowOf(emptyList())
  }

  private fun createViewModel(): TaskDetailViewModel =
    TaskDetailViewModel(
      savedStateHandle = savedStateHandle,
      taskRepository = taskRepository,
      projectRepository = projectRepository,
      taskCategoryRepository = categoryRepository,
    )

  @Test
  fun `task found emits Success state`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, title = "Write tests")
    every { taskRepository.observeById(TEST_TASK_ID) } returns flowOf(task)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskDetailUiState.Success
      state.task.taskId shouldBe TEST_TASK_ID
      state.task.title shouldBe "Write tests"
    }
  }

  @Test
  fun `task not found emits Error state`() = runTest {
    every { taskRepository.observeById(TEST_TASK_ID) } returns flowOf(null)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskDetailUiState.Error
      state.message shouldBe "Task not found"
    }
  }

  @Test
  fun `task with project resolves project in Success state`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, projectId = TEST_PROJECT_ID)
    val project = makeProject(projectId = TEST_PROJECT_ID, name = "Work")

    every { taskRepository.observeById(TEST_TASK_ID) } returns flowOf(task)
    every { projectRepository.observeById(TEST_PROJECT_ID) } returns flowOf(project)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskDetailUiState.Success
      state.project shouldNotBe null
      state.project?.projectId shouldBe TEST_PROJECT_ID
    }
  }

  @Test
  fun `task without project has null project in Success state`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, projectId = null)
    every { taskRepository.observeById(TEST_TASK_ID) } returns flowOf(task)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskDetailUiState.Success
      state.project shouldBe null
    }
  }

  @Test
  fun `task with category resolves category in Success state`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, categoryId = TEST_CATEGORY_ID)
    val category = makeCategory(categoryId = TEST_CATEGORY_ID, name = "Personal")

    every { taskRepository.observeById(TEST_TASK_ID) } returns flowOf(task)
    every { categoryRepository.observeAll() } returns flowOf(listOf(category))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskDetailUiState.Success
      state.category shouldNotBe null
      state.category?.categoryId shouldBe TEST_CATEGORY_ID
    }
  }

  @Test
  fun `task without category has null category in Success state`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, categoryId = null)
    every { taskRepository.observeById(TEST_TASK_ID) } returns flowOf(task)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskDetailUiState.Success
      state.category shouldBe null
    }
  }

  @Test
  fun `toggleComplete changes TODO task to DONE`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, status = TaskStatus.TODO)
    every { taskRepository.observeById(TEST_TASK_ID) } returns flowOf(task)
    coEvery { taskRepository.upsert(any()) } just runs

    val viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem() // Success
      viewModel.toggleComplete()
    }

    coVerify { taskRepository.upsert(match { it.status == TaskStatus.DONE }) }
  }

  @Test
  fun `toggleComplete changes DONE task back to TODO`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, status = TaskStatus.DONE)
    every { taskRepository.observeById(TEST_TASK_ID) } returns flowOf(task)
    coEvery { taskRepository.upsert(any()) } just runs

    val viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.toggleComplete()
    }

    coVerify { taskRepository.upsert(match { it.status == TaskStatus.TODO }) }
  }

  @Test
  fun `deleteTask delegates to repository delete`() = runTest {
    coEvery { taskRepository.delete(any()) } just runs

    val viewModel = createViewModel()
    viewModel.deleteTask()

    coVerify { taskRepository.delete(TEST_TASK_ID) }
  }

  @Test
  fun `repository error emits Error state`() = runTest {
    every { taskRepository.observeById(TEST_TASK_ID) } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException("Stream failed") }

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskDetailUiState.Error
      state.message shouldBe "Stream failed"
    }
  }

  @Test
  fun `uiState type is TaskDetailUiState`() = runTest {
    val viewModel = createViewModel()
    viewModel.uiState.value.shouldBeInstanceOf<TaskDetailUiState>()
  }
}
