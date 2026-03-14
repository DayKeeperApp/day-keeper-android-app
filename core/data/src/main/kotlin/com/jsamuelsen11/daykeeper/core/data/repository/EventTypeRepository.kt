package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.calendar.EventType
import kotlinx.coroutines.flow.Flow

public interface EventTypeRepository {
  public fun observeAll(): Flow<List<EventType>>

  public suspend fun getById(eventTypeId: String): EventType?

  public suspend fun upsert(eventType: EventType)

  public suspend fun delete(eventTypeId: String)
}
