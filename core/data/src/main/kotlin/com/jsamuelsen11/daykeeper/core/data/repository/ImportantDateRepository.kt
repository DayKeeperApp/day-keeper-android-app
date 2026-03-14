package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.people.ImportantDate
import kotlinx.coroutines.flow.Flow

public interface ImportantDateRepository {
  public fun observeByPerson(personId: String): Flow<List<ImportantDate>>

  public suspend fun getById(importantDateId: String): ImportantDate?

  public suspend fun upsert(importantDate: ImportantDate)

  public suspend fun delete(importantDateId: String)
}
