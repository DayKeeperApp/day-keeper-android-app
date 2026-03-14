package com.jsamuelsen11.daykeeper.core.data.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.people.AddressEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.ContactMethodEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.ImportantDateEntity
import com.jsamuelsen11.daykeeper.core.database.entity.people.PersonEntity
import com.jsamuelsen11.daykeeper.core.model.people.Address
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethod
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethodType
import com.jsamuelsen11.daykeeper.core.model.people.ImportantDate
import com.jsamuelsen11.daykeeper.core.model.people.Person

public fun PersonEntity.toDomain(): Person =
  Person(
    personId = personId,
    spaceId = spaceId,
    tenantId = tenantId,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun Person.toEntity(): PersonEntity =
  PersonEntity(
    personId = personId,
    spaceId = spaceId,
    tenantId = tenantId,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun ContactMethodEntity.toDomain(): ContactMethod =
  ContactMethod(
    contactMethodId = contactMethodId,
    personId = personId,
    type = ContactMethodType.valueOf(type),
    value = value,
    label = label,
    isPrimary = isPrimary,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun ContactMethod.toEntity(): ContactMethodEntity =
  ContactMethodEntity(
    contactMethodId = contactMethodId,
    personId = personId,
    type = type.name,
    value = value,
    label = label,
    isPrimary = isPrimary,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun AddressEntity.toDomain(): Address =
  Address(
    addressId = addressId,
    personId = personId,
    label = label,
    street = street,
    city = city,
    state = state,
    postalCode = postalCode,
    country = country,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun Address.toEntity(): AddressEntity =
  AddressEntity(
    addressId = addressId,
    personId = personId,
    label = label,
    street = street,
    city = city,
    state = state,
    postalCode = postalCode,
    country = country,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun ImportantDateEntity.toDomain(): ImportantDate =
  ImportantDate(
    importantDateId = importantDateId,
    personId = personId,
    label = label,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun ImportantDate.toEntity(): ImportantDateEntity =
  ImportantDateEntity(
    importantDateId = importantDateId,
    personId = personId,
    label = label,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )
