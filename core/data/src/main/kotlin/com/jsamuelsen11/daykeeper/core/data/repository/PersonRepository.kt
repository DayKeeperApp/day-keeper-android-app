package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.people.Person
import kotlinx.coroutines.flow.Flow

public interface PersonRepository {
  public fun observeById(personId: String): Flow<Person?>

  public fun observeBySpace(spaceId: String): Flow<List<Person>>

  public suspend fun getById(personId: String): Person?

  public suspend fun upsert(person: Person)

  public suspend fun delete(personId: String)
}
