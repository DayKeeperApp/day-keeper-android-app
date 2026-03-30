package com.jsamuelsen11.daykeeper.feature.tasks.list

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatus
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatusProvider
import com.jsamuelsen11.daykeeper.core.model.task.Priority
import com.jsamuelsen11.daykeeper.core.model.task.TaskStatus
import com.jsamuelsen11.daykeeper.feature.tasks.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_CATEGORY_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_PROJECT_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_SPACE_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_TASK_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_TASK_ID_2
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_UPDATED_AT
import com.jsamuelsen11.daykeeper.feature.tasks.makeCategory
import com.jsamuelsen11.daykeeper.feature.tasks.makeProject
import com.jsamuelsen11.daykeeper.feature.tasks.makeTask
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
class TaskListViewModelTest {

  private val taskRepository = mockk<TaskRepository>()
  private val projectRepository = mockk<ProjectRepository>()
  private val categoryRepository = mockk<TaskCategoryRepository>()
  private val syncStatusProvider =
    mockk<SyncStatusProvider> {
      every { syncStatus } returns kotlinx.coroutines.flow.MutableStateFlow(SyncStatus.Idle)
    }

  @BeforeEach
  fun setUp() {
    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(emptyList())
    every { projectRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(emptyList())
    every { categoryRepository.observeAll() } returns flowOf(emptyList())
  }

  private fun createViewModel(): TaskListViewModel =
    TaskListViewModel(
      taskRepository = taskRepository,
      projectRepository = projectRepository,
      taskCategoryRepository = categoryRepository,
      syncStatusProvider = syncStatusProvider,
    )

  @Test
  fun `empty repository emits Success with empty items`() = runTest {
    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskListUiState.Success
      state.items shouldBe emptyList()
      state.completedItems shouldBe emptyList()
    }
  }

  @Test
  fun `active task appears in items and not in completedItems`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, status = TaskStatus.TODO)
    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(task))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskListUiState.Success
      state.items.size shouldBe 1
      state.items.first().task.taskId shouldBe TEST_TASK_ID
      state.completedItems shouldBe emptyList()
    }
  }

  @Test
  fun `done task appears in completedItems and not in items`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, status = TaskStatus.DONE)
    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(task))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskListUiState.Success
      state.items shouldBe emptyList()
      state.completedItems.size shouldBe 1
      state.completedItems.first().task.taskId shouldBe TEST_TASK_ID
    }
  }

  @Test
  fun `task enriched with projectName and categoryName`() = runTest {
    val task =
      makeTask(taskId = TEST_TASK_ID, projectId = TEST_PROJECT_ID, categoryId = TEST_CATEGORY_ID)
    val project = makeProject(projectId = TEST_PROJECT_ID, name = "Work")
    val category = makeCategory(categoryId = TEST_CATEGORY_ID, name = "Personal")

    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(task))
    every { projectRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(project))
    every { categoryRepository.observeAll() } returns flowOf(listOf(category))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskListUiState.Success
      val item = state.items.first()
      item.projectName shouldBe "Work"
      item.categoryName shouldBe "Personal"
    }
  }

  @Test
  fun `categories are exposed in Success state`() = runTest {
    val category = makeCategory(categoryId = TEST_CATEGORY_ID, name = "Personal")
    every { categoryRepository.observeAll() } returns flowOf(listOf(category))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskListUiState.Success
      state.categories.size shouldBe 1
      state.categories.first().categoryId shouldBe TEST_CATEGORY_ID
    }
  }

  @Test
  fun `setViewMode BY_PROJECT builds project groups`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, projectId = TEST_PROJECT_ID)
    val project = makeProject(projectId = TEST_PROJECT_ID, name = "Work")

    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(task))
    every { projectRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(project))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem() // ALL_TASKS state
      viewModel.setViewMode(ViewMode.BY_PROJECT)
      val state = awaitItem() as TaskListUiState.Success
      state.viewMode shouldBe ViewMode.BY_PROJECT
      state.projectGroups.size shouldBe 1
      state.projectGroups.first().project.projectId shouldBe TEST_PROJECT_ID
    }
  }

  @Test
  fun `setStatusFilter filters out non-matching tasks`() = runTest {
    val todoTask = makeTask(taskId = TEST_TASK_ID, status = TaskStatus.TODO)
    val inProgressTask = makeTask(taskId = TEST_TASK_ID_2, status = TaskStatus.IN_PROGRESS)
    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns
      flowOf(listOf(todoTask, inProgressTask))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem() // unfiltered
      viewModel.setStatusFilter(setOf(TaskStatus.TODO))
      val state = awaitItem() as TaskListUiState.Success
      state.items.size shouldBe 1
      state.items.first().task.taskId shouldBe TEST_TASK_ID
    }
  }

  @Test
  fun `setPriorityFilter filters out non-matching tasks`() = runTest {
    val highTask = makeTask(taskId = TEST_TASK_ID, priority = Priority.HIGH)
    val lowTask = makeTask(taskId = TEST_TASK_ID_2, priority = Priority.LOW)
    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(highTask, lowTask))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.setPriorityFilter(setOf(Priority.HIGH))
      val state = awaitItem() as TaskListUiState.Success
      state.items.size shouldBe 1
      state.items.first().task.taskId shouldBe TEST_TASK_ID
    }
  }

  @Test
  fun `setCategoryFilter filters by category`() = runTest {
    val categorised = makeTask(taskId = TEST_TASK_ID, categoryId = TEST_CATEGORY_ID)
    val uncategorised = makeTask(taskId = TEST_TASK_ID_2, categoryId = null)
    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns
      flowOf(listOf(categorised, uncategorised))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.setCategoryFilter(TEST_CATEGORY_ID)
      val state = awaitItem() as TaskListUiState.Success
      state.items.size shouldBe 1
      state.items.first().task.taskId shouldBe TEST_TASK_ID
    }
  }

  @Test
  fun `toggleComplete changes TODO task to DONE`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, status = TaskStatus.TODO)
    coEvery { taskRepository.getById(TEST_TASK_ID) } returns task
    coEvery { taskRepository.upsert(any()) } just runs

    val viewModel = createViewModel()
    viewModel.toggleComplete(TEST_TASK_ID)

    coVerify { taskRepository.upsert(match { it.status == TaskStatus.DONE }) }
  }

  @Test
  fun `toggleComplete changes DONE task back to TODO`() = runTest {
    val task = makeTask(taskId = TEST_TASK_ID, status = TaskStatus.DONE)
    coEvery { taskRepository.getById(TEST_TASK_ID) } returns task
    coEvery { taskRepository.upsert(any()) } just runs

    val viewModel = createViewModel()
    viewModel.toggleComplete(TEST_TASK_ID)

    coVerify { taskRepository.upsert(match { it.status == TaskStatus.TODO }) }
  }

  @Test
  fun `deleteTask delegates to repository delete`() = runTest {
    coEvery { taskRepository.delete(any()) } just runs

    val viewModel = createViewModel()
    viewModel.deleteTask(TEST_TASK_ID)

    coVerify { taskRepository.delete(TEST_TASK_ID) }
  }

  @Test
  fun `deleted tasks are excluded from items`() = runTest {
    val deletedTask = makeTask(taskId = TEST_TASK_ID).copy(deletedAt = TEST_UPDATED_AT)
    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(listOf(deletedTask))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskListUiState.Success
      state.items shouldBe emptyList()
    }
  }

  @Test
  fun `repository error emits Error state`() = runTest {
    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException("DB error") }

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as TaskListUiState.Error
      state.message shouldBe "DB error"
    }
  }

  @Test
  fun `uiState type is TaskListUiState`() = runTest {
    val viewModel = createViewModel()
    viewModel.uiState.value.shouldBeInstanceOf<TaskListUiState>()
  }
}
