package com.jsamuelsen11.daykeeper.feature.tasks.category

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.feature.tasks.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_CATEGORY_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_CATEGORY_ID_2
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_SPACE_ID
import com.jsamuelsen11.daykeeper.feature.tasks.TEST_TASK_ID
import com.jsamuelsen11.daykeeper.feature.tasks.makeCategory
import com.jsamuelsen11.daykeeper.feature.tasks.makeTask
import io.kotest.matchers.shouldBe
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
class CategoryManagementViewModelTest {

  private val categoryRepository = mockk<TaskCategoryRepository>()
  private val taskRepository = mockk<TaskRepository>()

  @BeforeEach
  fun setUp() {
    every { categoryRepository.observeAll() } returns flowOf(emptyList())
    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns flowOf(emptyList())
  }

  private fun createViewModel(): CategoryManagementViewModel =
    CategoryManagementViewModel(
      taskCategoryRepository = categoryRepository,
      taskRepository = taskRepository,
    )

  @Test
  fun `empty repository emits Success with empty categories`() = runTest {
    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CategoryManagementUiState.Success
      state.categories shouldBe emptyList()
    }
  }

  @Test
  fun `categories appear in Success state`() = runTest {
    val category = makeCategory(categoryId = TEST_CATEGORY_ID, name = "Personal")
    every { categoryRepository.observeAll() } returns flowOf(listOf(category))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CategoryManagementUiState.Success
      state.categories.size shouldBe 1
      state.categories.first().category.categoryId shouldBe TEST_CATEGORY_ID
    }
  }

  @Test
  fun `task count per category is computed correctly`() = runTest {
    val category = makeCategory(categoryId = TEST_CATEGORY_ID)
    val taskWithCategory = makeTask(taskId = TEST_TASK_ID, categoryId = TEST_CATEGORY_ID)
    val taskWithoutCategory = makeTask(taskId = "other-task", categoryId = null)

    every { categoryRepository.observeAll() } returns flowOf(listOf(category))
    every { taskRepository.observeBySpace(TEST_SPACE_ID) } returns
      flowOf(listOf(taskWithCategory, taskWithoutCategory))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CategoryManagementUiState.Success
      state.categories.first().taskCount shouldBe 1
    }
  }

  @Test
  fun `system categories appear before user categories sorted by name`() = runTest {
    val userCat = makeCategory(categoryId = TEST_CATEGORY_ID, name = "Errands", isSystem = false)
    val systemCat = makeCategory(categoryId = TEST_CATEGORY_ID_2, name = "Work", isSystem = true)
    every { categoryRepository.observeAll() } returns flowOf(listOf(userCat, systemCat))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CategoryManagementUiState.Success
      state.categories.first().category.isSystem shouldBe true
      state.categories.last().category.isSystem shouldBe false
    }
  }

  @Test
  fun `createCategory calls upsert with trimmed name`() = runTest {
    val upsertSlot = slot<com.jsamuelsen11.daykeeper.core.model.task.TaskCategory>()
    coEvery { categoryRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = createViewModel()
    viewModel.createCategory("  Work  ", "#0000FF")

    coVerify { categoryRepository.upsert(any()) }
    upsertSlot.captured.name shouldBe "Work"
    upsertSlot.captured.normalizedName shouldBe "work"
    upsertSlot.captured.color shouldBe "#0000FF"
    upsertSlot.captured.isSystem shouldBe false
  }

  @Test
  fun `createCategory with null color creates category without color`() = runTest {
    val upsertSlot = slot<com.jsamuelsen11.daykeeper.core.model.task.TaskCategory>()
    coEvery { categoryRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = createViewModel()
    viewModel.createCategory("Personal", null)

    upsertSlot.captured.color shouldBe null
  }

  @Test
  fun `updateCategory upserts with updated name and color`() = runTest {
    val existing = makeCategory(categoryId = TEST_CATEGORY_ID, name = "Old Name")
    coEvery { categoryRepository.getById(TEST_CATEGORY_ID) } returns existing

    val upsertSlot = slot<com.jsamuelsen11.daykeeper.core.model.task.TaskCategory>()
    coEvery { categoryRepository.upsert(capture(upsertSlot)) } just runs

    val viewModel = createViewModel()
    viewModel.updateCategory(TEST_CATEGORY_ID, "New Name", "#123456")

    coVerify { categoryRepository.upsert(any()) }
    upsertSlot.captured.name shouldBe "New Name"
    upsertSlot.captured.normalizedName shouldBe "new name"
    upsertSlot.captured.color shouldBe "#123456"
    upsertSlot.captured.categoryId shouldBe TEST_CATEGORY_ID
  }

  @Test
  fun `updateCategory with missing category does not call upsert`() = runTest {
    coEvery { categoryRepository.getById(TEST_CATEGORY_ID) } returns null

    val viewModel = createViewModel()
    viewModel.updateCategory(TEST_CATEGORY_ID, "Name", null)

    coVerify(exactly = 0) { categoryRepository.upsert(any()) }
  }

  @Test
  fun `deleteCategory delegates to repository delete`() = runTest {
    coEvery { categoryRepository.delete(any()) } just runs

    val viewModel = createViewModel()
    viewModel.deleteCategory(TEST_CATEGORY_ID)

    coVerify { categoryRepository.delete(TEST_CATEGORY_ID) }
  }

  @Test
  fun `repository error emits Error state`() = runTest {
    every { categoryRepository.observeAll() } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException("Broken") }

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as CategoryManagementUiState.Error
      state.message shouldBe "Broken"
    }
  }

  @Test
  fun `uiState type is CategoryManagementUiState`() = runTest {
    val viewModel = createViewModel()
    viewModel.uiState.value.shouldBeInstanceOf<CategoryManagementUiState>()
  }
}
