package com.jsamuelsen11.daykeeper.feature.people.createedit

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
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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
class PersonCreateEditViewModelTest {

  private val personRepository = mockk<PersonRepository>()
  private val contactMethodRepository = mockk<ContactMethodRepository>()
  private val addressRepository = mockk<AddressRepository>()
  private val importantDateRepository = mockk<ImportantDateRepository>()

  @BeforeEach
  fun setUp() {
    // Edit-mode load stubs — safe defaults so individual tests only override what they need
    coEvery { personRepository.getById(TEST_PERSON_ID) } returns makePerson()
    every { contactMethodRepository.observeByPerson(TEST_PERSON_ID) } returns flowOf(emptyList())
    every { addressRepository.observeByPerson(TEST_PERSON_ID) } returns flowOf(emptyList())
    every { importantDateRepository.observeByPerson(TEST_PERSON_ID) } returns flowOf(emptyList())

    // Save stubs — return without side-effects by default
    coEvery { personRepository.upsert(any()) } just runs
    coEvery { contactMethodRepository.upsert(any()) } just runs
    coEvery { addressRepository.upsert(any()) } just runs
    coEvery { importantDateRepository.upsert(any()) } just runs
    coEvery { contactMethodRepository.delete(any()) } just runs
    coEvery { addressRepository.delete(any()) } just runs
    coEvery { importantDateRepository.delete(any()) } just runs
  }

  private fun createModeViewModel(): PersonCreateEditViewModel =
    PersonCreateEditViewModel(
      savedStateHandle = SavedStateHandle(),
      personRepository = personRepository,
      contactMethodRepository = contactMethodRepository,
      addressRepository = addressRepository,
      importantDateRepository = importantDateRepository,
    )

  private fun editModeViewModel(personId: String = TEST_PERSON_ID): PersonCreateEditViewModel =
    PersonCreateEditViewModel(
      savedStateHandle = SavedStateHandle(mapOf("personId" to personId)),
      personRepository = personRepository,
      contactMethodRepository = contactMethodRepository,
      addressRepository = addressRepository,
      importantDateRepository = importantDateRepository,
    )

  // --- Create mode ---

  @Test
  fun `create mode starts with empty Ready state`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val state = awaitItem() as PersonCreateEditUiState.Ready
      state.firstName shouldBe ""
      state.lastName shouldBe ""
      state.nickname shouldBe ""
      state.notes shouldBe ""
      state.contactMethods shouldBe emptyList()
      state.addresses shouldBe emptyList()
      state.importantDates shouldBe emptyList()
      state.isEditing shouldBe false
      state.isSaving shouldBe false
      state.firstNameError shouldBe null
      state.lastNameError shouldBe null
    }
  }

  // --- Edit mode ---

  @Test
  fun `edit mode loads existing person data`() = runTest {
    val person =
      makePerson(personId = TEST_PERSON_ID, firstName = "Jane", lastName = "Doe", nickname = "Jay")
    coEvery { personRepository.getById(TEST_PERSON_ID) } returns person

    val contactMethod = makeContactMethod(contactMethodId = TEST_CONTACT_METHOD_ID)
    val address = makeAddress(addressId = TEST_ADDRESS_ID)
    val importantDate = makeImportantDate(importantDateId = TEST_IMPORTANT_DATE_ID)

    every { contactMethodRepository.observeByPerson(TEST_PERSON_ID) } returns
      flowOf(listOf(contactMethod))
    every { addressRepository.observeByPerson(TEST_PERSON_ID) } returns flowOf(listOf(address))
    every { importantDateRepository.observeByPerson(TEST_PERSON_ID) } returns
      flowOf(listOf(importantDate))

    val viewModel = editModeViewModel()

    viewModel.uiState.test {
      // Loading transitions immediately to Ready with UnconfinedTestDispatcher
      val state = awaitItem() as PersonCreateEditUiState.Ready
      state.firstName shouldBe "Jane"
      state.lastName shouldBe "Doe"
      state.nickname shouldBe "Jay"
      state.isEditing shouldBe true
      state.contactMethods shouldHaveSize 1
      state.contactMethods.first().existingId shouldBe TEST_CONTACT_METHOD_ID
      state.addresses shouldHaveSize 1
      state.addresses.first().existingId shouldBe TEST_ADDRESS_ID
      state.importantDates shouldHaveSize 1
      state.importantDates.first().existingId shouldBe TEST_IMPORTANT_DATE_ID
    }
  }

  // --- Field change handlers ---

  @Test
  fun `onFirstNameChanged updates state and clears error`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem() // initial Ready

      // Trigger a validation error first
      viewModel.onSave()
      val errorState = awaitItem() as PersonCreateEditUiState.Ready
      errorState.firstNameError shouldBe "First name is required"

      // Typing should clear the error
      viewModel.onFirstNameChanged("Alice")
      val clearedState = awaitItem() as PersonCreateEditUiState.Ready
      clearedState.firstName shouldBe "Alice"
      clearedState.firstNameError shouldBe null
    }
  }

  @Test
  fun `onLastNameChanged updates state and clears error`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem() // initial Ready

      viewModel.onFirstNameChanged("Alice")
      awaitItem()

      // Trigger last name validation error
      viewModel.onSave()
      val errorState = awaitItem() as PersonCreateEditUiState.Ready
      errorState.lastNameError shouldBe "Last name is required"

      // Typing should clear the error
      viewModel.onLastNameChanged("Smith")
      val clearedState = awaitItem() as PersonCreateEditUiState.Ready
      clearedState.lastName shouldBe "Smith"
      clearedState.lastNameError shouldBe null
    }
  }

  // --- Validation ---

  @Test
  fun `onSave with blank firstName sets error`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem() // initial Ready

      viewModel.onSave()

      val state = awaitItem() as PersonCreateEditUiState.Ready
      state.firstNameError shouldBe "First name is required"
    }

    coVerify(exactly = 0) { personRepository.upsert(any()) }
  }

  @Test
  fun `onSave with blank lastName sets error`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()

      viewModel.onFirstNameChanged("Alice")
      awaitItem()

      viewModel.onSave()

      val state = awaitItem() as PersonCreateEditUiState.Ready
      state.lastNameError shouldBe "Last name is required"
    }

    coVerify(exactly = 0) { personRepository.upsert(any()) }
  }

  // --- onSave create mode ---

  @Test
  fun `onSave in create mode calls personRepository upsert and emits Saved`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem()
        viewModel.onFirstNameChanged("Alice")
        awaitItem()
        viewModel.onLastNameChanged("Smith")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe PersonCreateEditEvent.Saved
    }

    coVerify { personRepository.upsert(any()) }
  }

  @Test
  fun `onSave in create mode upserts person with trimmed names`() = runTest {
    val personSlot = slot<com.jsamuelsen11.daykeeper.core.model.people.Person>()
    coEvery { personRepository.upsert(capture(personSlot)) } just runs

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onFirstNameChanged("  Alice  ")
      awaitItem()
      viewModel.onLastNameChanged("  Smith  ")
      awaitItem()
      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    personSlot.captured.firstName shouldBe "Alice"
    personSlot.captured.lastName shouldBe "Smith"
  }

  // --- onSave edit mode ---

  @Test
  fun `onSave in edit mode updates existing person`() = runTest {
    coEvery { personRepository.getById(TEST_PERSON_ID) } returns
      makePerson(firstName = "Old", lastName = "Name")

    val personSlot = slot<com.jsamuelsen11.daykeeper.core.model.people.Person>()
    coEvery { personRepository.upsert(capture(personSlot)) } just runs

    val viewModel = editModeViewModel()

    viewModel.events.test {
      viewModel.uiState.test {
        awaitItem() // Ready with loaded data
        viewModel.onFirstNameChanged("New")
        awaitItem()
        viewModel.onSave()
        cancelAndIgnoreRemainingEvents()
      }

      awaitItem() shouldBe PersonCreateEditEvent.Saved
    }

    personSlot.captured.personId shouldBe TEST_PERSON_ID
    personSlot.captured.firstName shouldBe "New"
    personSlot.captured.lastName shouldBe "Name"
  }

  // --- Dynamic sections ---

  @Test
  fun `addContactMethod appends entry`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val before = awaitItem() as PersonCreateEditUiState.Ready
      before.contactMethods shouldHaveSize 0

      viewModel.addContactMethod()

      val after = awaitItem() as PersonCreateEditUiState.Ready
      after.contactMethods shouldHaveSize 1
    }
  }

  @Test
  fun `removeContactMethod removes entry by tempId`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()

      viewModel.addContactMethod()
      val withOne = awaitItem() as PersonCreateEditUiState.Ready
      withOne.contactMethods shouldHaveSize 1

      val tempId = withOne.contactMethods.first().tempId

      viewModel.removeContactMethod(tempId)

      val afterRemove = awaitItem() as PersonCreateEditUiState.Ready
      afterRemove.contactMethods shouldHaveSize 0
    }
  }

  @Test
  fun `addAddress appends entry`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val before = awaitItem() as PersonCreateEditUiState.Ready
      before.addresses shouldHaveSize 0

      viewModel.addAddress()

      val after = awaitItem() as PersonCreateEditUiState.Ready
      after.addresses shouldHaveSize 1
    }
  }

  @Test
  fun `addImportantDate appends entry`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      val before = awaitItem() as PersonCreateEditUiState.Ready
      before.importantDates shouldHaveSize 0

      viewModel.addImportantDate()

      val after = awaitItem() as PersonCreateEditUiState.Ready
      after.importantDates shouldHaveSize 1
    }
  }

  // --- onSave saves all sub-entities ---

  @Test
  fun `onSave saves all sub-entities`() = runTest {
    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()

      viewModel.onFirstNameChanged("Alice")
      awaitItem()
      viewModel.onLastNameChanged("Smith")
      awaitItem()
      viewModel.addContactMethod()
      awaitItem()
      viewModel.addAddress()
      awaitItem()
      viewModel.addImportantDate()
      awaitItem()

      viewModel.onSave()
      cancelAndIgnoreRemainingEvents()
    }

    coVerify { personRepository.upsert(any()) }
    coVerify { contactMethodRepository.upsert(any()) }
    coVerify { addressRepository.upsert(any()) }
    coVerify { importantDateRepository.upsert(any()) }
  }

  // --- onSave failure ---

  @Test
  fun `onSave repository failure resets isSaving with error`() = runTest {
    coEvery { personRepository.upsert(any()) } throws RuntimeException("DB write failed")

    val viewModel = createModeViewModel()

    viewModel.uiState.test {
      awaitItem()
      viewModel.onFirstNameChanged("Alice")
      awaitItem()
      viewModel.onLastNameChanged("Smith")
      awaitItem()
      viewModel.onSave()

      // May transition through isSaving=true then reset — accept either ordering
      val savingState = awaitItem() as PersonCreateEditUiState.Ready
      if (savingState.isSaving) {
        val resetState = awaitItem() as PersonCreateEditUiState.Ready
        resetState.isSaving shouldBe false
        resetState.firstNameError shouldBe "DB write failed"
      } else {
        savingState.isSaving shouldBe false
        savingState.firstNameError shouldBe "DB write failed"
      }
    }
  }
}
