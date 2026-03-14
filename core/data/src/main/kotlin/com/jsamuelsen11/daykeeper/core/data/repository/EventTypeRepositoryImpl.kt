package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.EventTypeDao
import com.jsamuelsen11.daykeeper.core.model.calendar.EventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class EventTypeRepositoryImpl(private val dao: EventTypeDao) : EventTypeRepository {
  public override fun observeAll(): Flow<List<EventType>> =
    dao.observeAll().map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(eventTypeId: String): EventType? =
    dao.getById(eventTypeId)?.toDomain()

  public override suspend fun upsert(eventType: EventType) {
    dao.upsert(eventType.toEntity())
  }

  public override suspend fun delete(eventTypeId: String) {
    dao.hardDelete(eventTypeId)
  }
}
