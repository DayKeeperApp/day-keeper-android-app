package com.jsamuelsen11.daykeeper.feature.people.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.AddressRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ContactMethodRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ImportantDateRepository
import com.jsamuelsen11.daykeeper.core.data.repository.PersonRepository
import com.jsamuelsen11.daykeeper.feature.people.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.people.TEST_ADDRESS_ID
import com.jsamuelsen11.daykeeper.feature.people.TEST_CONTACT_METHOD_ID
import com.jsamuelsen11.daykeeper.feature.people.TEST_IMPORTANT_DATE_ID
import com.jsamuelsen11.daykeeper.feature.people.TEST_PERSON_ID
import com.jsamuelsen11.daykeeper.feature.people.makeAddress
import com.jsamuelsen11.daykeeper.feature.people.makeContactMethod
import com.jsamuelsen11.daykeeper.feature.people.makeImportantDate
import com.jsamuelsen11.daykeeper.feature.people.makePerson
import io.kotest.matchers.shouldBe
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
class PersonDetailViewModelTest {

  private val personRepository = mockk<PersonRepository>()
  private val contactMethodRepository = mockk<ContactMethodRepository>()
  private val addressRepository = mockk<AddressRepository>()
  private val importantDateRepository = mockk<ImportantDateRepository>()

  private val savedStateHandle = SavedStateHandle(mapOf("personId" to TEST_PERSON_ID))

  @BeforeEach
  fun setUp() {
    every { personRepository.observeById(TEST_PERSON_ID) } returns flowOf(makePerson())
    every { contactMethodRepository.observeByPerson(TEST_PERSON_ID) } returns flowOf(emptyList())
    every { addressRepository.observeByPerson(TEST_PERSON_ID) } returns flowOf(emptyList())
    every { importantDateRepository.observeByPerson(TEST_PERSON_ID) } returns flowOf(emptyList())
  }

  private fun createViewModel(): PersonDetailViewModel =
    PersonDetailViewModel(
      savedStateHandle = savedStateHandle,
      personRepository = personRepository,
      contactMethodRepository = contactMethodRepository,
      addressRepository = addressRepository,
      importantDateRepository = importantDateRepository,
    )

  // --- UiState shape ---

  @Test
  fun `loads person with all related entities`() = runTest {
    val person = makePerson()
    val contactMethod = makeContactMethod()
    val address = makeAddress()
    val importantDate = makeImportantDate()

    every { personRepository.observeById(TEST_PERSON_ID) } returns flowOf(person)
    every { contactMethodRepository.observeByPerson(TEST_PERSON_ID) } returns
      flowOf(listOf(contactMethod))
    every { addressRepository.observeByPerson(TEST_PERSON_ID) } returns flowOf(listOf(address))
    every { importantDateRepository.observeByPerson(TEST_PERSON_ID) } returns
      flowOf(listOf(importantDate))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as PersonDetailUiState.Success
      state.person shouldBe person
      state.contactMethods shouldBe listOf(contactMethod)
      state.addresses shouldBe listOf(address)
      state.importantDates shouldBe listOf(importantDate)
    }
  }

  @Test
  fun `missing person emits Error state`() = runTest {
    every { personRepository.observeById(TEST_PERSON_ID) } returns flowOf(null)

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as PersonDetailUiState.Error
      state.message shouldBe "Person not found"
    }
  }

  @Test
  fun `repository error emits Error state`() = runTest {
    val errorMessage = "Database unavailable"
    every { personRepository.observeById(TEST_PERSON_ID) } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException(errorMessage) }

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as PersonDetailUiState.Error
      state.message shouldBe errorMessage
    }
  }

  @Test
  fun `repository error with null message emits unknown error`() = runTest {
    every { personRepository.observeById(TEST_PERSON_ID) } returns
      kotlinx.coroutines.flow.flow { throw IllegalStateException(null as String?) }

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as PersonDetailUiState.Error
      state.message shouldBe "Unknown error"
    }
  }

  @Test
  fun `deletePerson deletes all entities and emits Deleted event`() = runTest {
    val contactMethod = makeContactMethod(contactMethodId = TEST_CONTACT_METHOD_ID)
    val address = makeAddress(addressId = TEST_ADDRESS_ID)
    val importantDate = makeImportantDate(importantDateId = TEST_IMPORTANT_DATE_ID)

    every { contactMethodRepository.observeByPerson(TEST_PERSON_ID) } returns
      flowOf(listOf(contactMethod))
    every { addressRepository.observeByPerson(TEST_PERSON_ID) } returns flowOf(listOf(address))
    every { importantDateRepository.observeByPerson(TEST_PERSON_ID) } returns
      flowOf(listOf(importantDate))

    coEvery { contactMethodRepository.delete(any()) } just runs
    coEvery { addressRepository.delete(any()) } just runs
    coEvery { importantDateRepository.delete(any()) } just runs
    coEvery { personRepository.delete(any()) } just runs

    val viewModel = createViewModel()

    viewModel.events.test {
      // Allow uiState to settle to Success so deletePerson can read sub-entities
      viewModel.uiState.test {
        awaitItem() as PersonDetailUiState.Success
        viewModel.deletePerson()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe PersonDetailEvent.Deleted
    }

    coVerify { contactMethodRepository.delete(TEST_CONTACT_METHOD_ID) }
    coVerify { addressRepository.delete(TEST_ADDRESS_ID) }
    coVerify { importantDateRepository.delete(TEST_IMPORTANT_DATE_ID) }
    coVerify { personRepository.delete(TEST_PERSON_ID) }
  }

  @Test
  fun `reactive updates when data changes`() = runTest {
    val personFlow =
      MutableStateFlow<com.jsamuelsen11.daykeeper.core.model.people.Person?>(makePerson())
    every { personRepository.observeById(TEST_PERSON_ID) } returns personFlow

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val initial = awaitItem() as PersonDetailUiState.Success
      initial.person.firstName shouldBe "John"

      personFlow.value = makePerson(firstName = "Jonathan")

      val updated = awaitItem() as PersonDetailUiState.Success
      updated.person.firstName shouldBe "Jonathan"
    }
  }
}
