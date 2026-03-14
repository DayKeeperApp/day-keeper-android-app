package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.ContactMethodDao
import com.jsamuelsen11.daykeeper.core.model.people.ContactMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class ContactMethodRepositoryImpl(private val dao: ContactMethodDao) :
  ContactMethodRepository {
  public override fun observeByPerson(personId: String): Flow<List<ContactMethod>> =
    dao.observeByPerson(personId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(contactMethodId: String): ContactMethod? =
    dao.getById(contactMethodId)?.toDomain()

  public override suspend fun upsert(contactMethod: ContactMethod) {
    dao.upsert(contactMethod.toEntity())
  }

  public override suspend fun delete(contactMethodId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(contactMethodId, deletedAt = now, updatedAt = now)
  }
}
