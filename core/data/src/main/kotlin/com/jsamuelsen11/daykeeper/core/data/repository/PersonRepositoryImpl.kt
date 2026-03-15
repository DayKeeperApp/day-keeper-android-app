package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.PersonDao
import com.jsamuelsen11.daykeeper.core.model.people.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class PersonRepositoryImpl(private val dao: PersonDao) : PersonRepository {
  public override fun observeById(personId: String): Flow<Person?> =
    dao.observeById(personId).map { it?.toDomain() }

  public override fun observeBySpace(spaceId: String): Flow<List<Person>> =
    dao.observeBySpace(spaceId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(personId: String): Person? = dao.getById(personId)?.toDomain()

  public override suspend fun upsert(person: Person) {
    dao.upsert(person.toEntity())
  }

  public override suspend fun delete(personId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(personId, deletedAt = now, updatedAt = now)
  }
}
