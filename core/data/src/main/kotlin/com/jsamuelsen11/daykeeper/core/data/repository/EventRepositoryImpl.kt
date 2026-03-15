package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.EventDao
import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class EventRepositoryImpl(private val dao: EventDao) : EventRepository {
  public override fun observeById(eventId: String): Flow<Event?> =
    dao.observeById(eventId).map { it?.toDomain() }

  public override fun observeByCalendar(calendarId: String): Flow<List<Event>> =
    dao.observeByCalendar(calendarId).map { list -> list.map { it.toDomain() } }

  public override fun observeByCalendarAndRange(
    calendarId: String,
    startMillis: Long,
    endMillis: Long,
  ): Flow<List<Event>> =
    dao.observeByCalendarAndRange(calendarId, startMillis, endMillis).map { list ->
      list.map { it.toDomain() }
    }

  public override fun observeAllDayByCalendarAndRange(
    calendarId: String,
    startDate: String,
    endDate: String,
  ): Flow<List<Event>> =
    dao.observeAllDayByCalendarAndRange(calendarId, startDate, endDate).map { list ->
      list.map { it.toDomain() }
    }

  public override suspend fun getById(eventId: String): Event? = dao.getById(eventId)?.toDomain()

  public override suspend fun upsert(event: Event) {
    dao.upsert(event.toEntity())
  }

  public override suspend fun delete(eventId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(eventId, deletedAt = now, updatedAt = now)
  }
}
