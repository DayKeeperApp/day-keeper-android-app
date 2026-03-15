package com.jsamuelsen11.daykeeper.feature.people

import com.jsamuelsen11.daykeeper.core.model.people.Address
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethod
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethodType
import com.jsamuelsen11.daykeeper.core.model.people.ImportantDate
import com.jsamuelsen11.daykeeper.core.model.people.Person

internal const val TEST_PERSON_ID = "test-person-id"
internal const val TEST_PERSON_ID_2 = "test-person-id-2"
internal const val TEST_SPACE_ID = "test-space-id"
internal const val TEST_TENANT_ID = "test-tenant-id"
internal const val TEST_CONTACT_METHOD_ID = "test-cm-id"
internal const val TEST_CONTACT_METHOD_ID_2 = "test-cm-id-2"
internal const val TEST_ADDRESS_ID = "test-address-id"
internal const val TEST_IMPORTANT_DATE_ID = "test-date-id"
internal const val TEST_CREATED_AT = 1_000L
internal const val TEST_UPDATED_AT = 2_000L

internal fun makePerson(
  personId: String = TEST_PERSON_ID,
  firstName: String = "John",
  lastName: String = "Doe",
  nickname: String? = null,
  notes: String? = null,
  spaceId: String = TEST_SPACE_ID,
  tenantId: String = TEST_TENANT_ID,
): Person =
  Person(
    personId = personId,
    spaceId = spaceId,
    tenantId = tenantId,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    notes = notes,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeContactMethod(
  contactMethodId: String = TEST_CONTACT_METHOD_ID,
  personId: String = TEST_PERSON_ID,
  type: ContactMethodType = ContactMethodType.PHONE,
  value: String = "555-1234",
  label: String = "Mobile",
  isPrimary: Boolean = true,
): ContactMethod =
  ContactMethod(
    contactMethodId = contactMethodId,
    personId = personId,
    type = type,
    value = value,
    label = label,
    isPrimary = isPrimary,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeAddress(
  addressId: String = TEST_ADDRESS_ID,
  personId: String = TEST_PERSON_ID,
  label: String = "Home",
  street: String? = "123 Main St",
  city: String? = "Springfield",
  state: String? = "IL",
  postalCode: String? = "62701",
  country: String? = "US",
): Address =
  Address(
    addressId = addressId,
    personId = personId,
    label = label,
    street = street,
    city = city,
    state = state,
    postalCode = postalCode,
    country = country,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )

internal fun makeImportantDate(
  importantDateId: String = TEST_IMPORTANT_DATE_ID,
  personId: String = TEST_PERSON_ID,
  label: String = "Birthday",
  date: String = "1990-01-15",
): ImportantDate =
  ImportantDate(
    importantDateId = importantDateId,
    personId = personId,
    label = label,
    date = date,
    createdAt = TEST_CREATED_AT,
    updatedAt = TEST_UPDATED_AT,
  )
