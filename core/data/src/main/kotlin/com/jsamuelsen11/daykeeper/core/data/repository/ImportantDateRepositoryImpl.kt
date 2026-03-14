package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.ImportantDateDao
import com.jsamuelsen11.daykeeper.core.model.people.ImportantDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class ImportantDateRepositoryImpl(private val dao: ImportantDateDao) :
  ImportantDateRepository {
  public override fun observeByPerson(personId: String): Flow<List<ImportantDate>> =
    dao.observeByPerson(personId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(importantDateId: String): ImportantDate? =
    dao.getById(importantDateId)?.toDomain()

  public override suspend fun upsert(importantDate: ImportantDate) {
    dao.upsert(importantDate.toEntity())
  }

  public override suspend fun delete(importantDateId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(importantDateId, deletedAt = now, updatedAt = now)
  }
}
