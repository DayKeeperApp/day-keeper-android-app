package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.EventReminderDao
import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class EventReminderRepositoryImpl(private val dao: EventReminderDao) :
  EventReminderRepository {
  public override fun observeByEvent(eventId: String): Flow<List<EventReminder>> =
    dao.observeByEvent(eventId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(reminderId: String): EventReminder? =
    dao.getById(reminderId)?.toDomain()

  public override suspend fun upsert(reminder: EventReminder) {
    dao.upsert(reminder.toEntity())
  }

  public override suspend fun delete(reminderId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(reminderId, deletedAt = now, updatedAt = now)
  }

  public override suspend fun getAllActive(): List<EventReminder> =
    dao.getAllActive().map { it.toDomain() }
}
