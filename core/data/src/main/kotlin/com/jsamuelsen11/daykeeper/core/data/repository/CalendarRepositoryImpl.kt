package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.CalendarDao
import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class CalendarRepositoryImpl(private val dao: CalendarDao) : CalendarRepository {
  public override fun observeById(calendarId: String): Flow<Calendar?> =
    dao.observeById(calendarId).map { it?.toDomain() }

  public override fun observeBySpace(spaceId: String): Flow<List<Calendar>> =
    dao.observeBySpace(spaceId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(calendarId: String): Calendar? =
    dao.getById(calendarId)?.toDomain()

  public override suspend fun upsert(calendar: Calendar) {
    dao.upsert(calendar.toEntity())
  }

  public override suspend fun delete(calendarId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(calendarId, deletedAt = now, updatedAt = now)
  }
}
