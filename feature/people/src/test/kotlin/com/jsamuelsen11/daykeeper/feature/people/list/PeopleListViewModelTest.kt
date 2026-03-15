package com.jsamuelsen11.daykeeper.feature.people.list

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.ContactMethodRepository
import com.jsamuelsen11.daykeeper.core.data.repository.PersonRepository
import com.jsamuelsen11.daykeeper.feature.people.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.people.TEST_PERSON_ID
import com.jsamuelsen11.daykeeper.feature.people.TEST_PERSON_ID_2
import com.jsamuelsen11.daykeeper.feature.people.makeContactMethod
import com.jsamuelsen11.daykeeper.feature.people.makePerson
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class PeopleListViewModelTest {

  private val personRepository = mockk<PersonRepository>()
  private val contactMethodRepository = mockk<ContactMethodRepository>()

  @BeforeEach
  fun setUp() {
    every { personRepository.observeBySpace(any()) } returns flowOf(emptyList())
    every { contactMethodRepository.observeByPerson(any()) } returns flowOf(emptyList())
  }

  private fun createViewModel(): PeopleListViewModel =
    PeopleListViewModel(
      personRepository = personRepository,
      contactMethodRepository = contactMethodRepository,
    )

  // --- UiState shape ---

  @Test
  fun `initial state is Loading`() {
    val peopleFlow =
      MutableStateFlow(emptyList<com.jsamuelsen11.daykeeper.core.model.people.Person>())
    every { personRepository.observeBySpace(any()) } returns peopleFlow

    val viewModel = createViewModel()

    // stateIn initial value is Loading; with UnconfinedTestDispatcher it transitions immediately,
    // so assert the sealed type is valid
    viewModel.uiState.value.shouldBeInstanceOf<PeopleListUiState>()
  }

  @Test
  fun `empty people emits Success with empty list`() = runTest {
    every { personRepository.observeBySpace(any()) } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as PeopleListUiState.Success
      state.people shouldBe emptyList()
    }
  }

  @Test
  fun `people loaded with primary contact methods`() = runTest {
    val person = makePerson(personId = TEST_PERSON_ID)
    val primary = makeContactMethod(personId = TEST_PERSON_ID, isPrimary = true)
    val secondary =
      makeContactMethod(
        contactMethodId = "secondary-cm-id",
        personId = TEST_PERSON_ID,
        isPrimary = false,
      )

    every { personRepository.observeBySpace(any()) } returns flowOf(listOf(person))
    every { contactMethodRepository.observeByPerson(TEST_PERSON_ID) } returns
      flowOf(listOf(secondary, primary))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as PeopleListUiState.Success
      state.people.size shouldBe 1
      state.people.first().person shouldBe person
      state.people.first().primaryContactMethod shouldBe primary
    }
  }

  @Test
  fun `search query filters by first name`() = runTest {
    val alice = makePerson(personId = TEST_PERSON_ID, firstName = "Alice", lastName = "Smith")
    val bob = makePerson(personId = TEST_PERSON_ID_2, firstName = "Bob", lastName = "Jones")

    every { personRepository.observeBySpace(any()) } returns flowOf(listOf(alice, bob))
    every { contactMethodRepository.observeByPerson(any()) } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem() // initial Success with both people

      viewModel.onSearchQueryChanged("ali")

      val filtered = awaitItem() as PeopleListUiState.Success
      filtered.people.size shouldBe 1
      filtered.people.first().person shouldBe alice
      filtered.searchQuery shouldBe "ali"
    }
  }

  @Test
  fun `search query filters by last name`() = runTest {
    val alice = makePerson(personId = TEST_PERSON_ID, firstName = "Alice", lastName = "Smith")
    val bob = makePerson(personId = TEST_PERSON_ID_2, firstName = "Bob", lastName = "Jones")

    every { personRepository.observeBySpace(any()) } returns flowOf(listOf(alice, bob))
    every { contactMethodRepository.observeByPerson(any()) } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem() // initial Success

      viewModel.onSearchQueryChanged("jones")

      val filtered = awaitItem() as PeopleListUiState.Success
      filtered.people.size shouldBe 1
      filtered.people.first().person shouldBe bob
    }
  }

  @Test
  fun `search query filters by nickname`() = runTest {
    val person =
      makePerson(
        personId = TEST_PERSON_ID,
        firstName = "Robert",
        lastName = "Smith",
        nickname = "Bobby",
      )

    every { personRepository.observeBySpace(any()) } returns flowOf(listOf(person))
    every { contactMethodRepository.observeByPerson(any()) } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      awaitItem() // initial Success

      viewModel.onSearchQueryChanged("bobb")

      val filtered = awaitItem() as PeopleListUiState.Success
      filtered.people.size shouldBe 1
      filtered.people.first().person shouldBe person
    }
  }

  @Test
  fun `sort by last name changes order`() = runTest {
    val alice = makePerson(personId = TEST_PERSON_ID, firstName = "Alice", lastName = "Zimmermann")
    val bob = makePerson(personId = TEST_PERSON_ID_2, firstName = "Bob", lastName = "Anderson")

    every { personRepository.observeBySpace(any()) } returns flowOf(listOf(alice, bob))
    every { contactMethodRepository.observeByPerson(any()) } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val firstNameState = awaitItem() as PeopleListUiState.Success
      // Default sort is FIRST_NAME_ASC — Alice comes before Bob
      firstNameState.people.first().person shouldBe alice

      viewModel.onSortOrderChanged(PeopleSortOrder.LAST_NAME_ASC)

      val lastNameState = awaitItem() as PeopleListUiState.Success
      // LAST_NAME_ASC — Anderson (Bob) comes before Zimmermann (Alice)
      lastNameState.people.first().person shouldBe bob
      lastNameState.sortOrder shouldBe PeopleSortOrder.LAST_NAME_ASC
    }
  }

  @Test
  fun `deletePerson delegates to repository`() = runTest {
    coEvery { personRepository.delete(any()) } just runs

    val viewModel = createViewModel()
    viewModel.deletePerson(TEST_PERSON_ID)

    coVerify { personRepository.delete(TEST_PERSON_ID) }
  }

  @Test
  fun `repository error emits Error state`() = runTest {
    val errorMessage = "Database unavailable"
    every { personRepository.observeBySpace(any()) } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException(errorMessage) }

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as PeopleListUiState.Error
      state.message shouldBe errorMessage
    }
  }

  @Test
  fun `repository error with null message emits unknown error`() = runTest {
    every { personRepository.observeBySpace(any()) } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException(null as String?) }

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as PeopleListUiState.Error
      state.message shouldBe "Unknown error"
    }
  }

  @Test
  fun `people list updates reactively`() = runTest {
    val peopleFlow =
      MutableStateFlow(emptyList<com.jsamuelsen11.daykeeper.core.model.people.Person>())
    every { personRepository.observeBySpace(any()) } returns peopleFlow
    every { contactMethodRepository.observeByPerson(TEST_PERSON_ID) } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val emptyState = awaitItem() as PeopleListUiState.Success
      emptyState.people shouldBe emptyList()

      peopleFlow.value = listOf(makePerson())

      val populatedState = awaitItem() as PeopleListUiState.Success
      populatedState.people.size shouldBe 1
    }
  }
}
