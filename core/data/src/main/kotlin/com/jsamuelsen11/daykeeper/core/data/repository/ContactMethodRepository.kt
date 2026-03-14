package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.people.ContactMethod
import kotlinx.coroutines.flow.Flow

public interface ContactMethodRepository {
  public fun observeByPerson(personId: String): Flow<List<ContactMethod>>

  public suspend fun getById(contactMethodId: String): ContactMethod?

  public suspend fun upsert(contactMethod: ContactMethod)

  public suspend fun delete(contactMethodId: String)
}
