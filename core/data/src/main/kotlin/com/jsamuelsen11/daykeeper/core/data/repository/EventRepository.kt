package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.calendar.Event
import kotlinx.coroutines.flow.Flow

public interface EventRepository {
  public fun observeById(eventId: String): Flow<Event?>

  public fun observeByCalendar(calendarId: String): Flow<List<Event>>

  public fun observeByCalendarAndRange(
    calendarId: String,
    startMillis: Long,
    endMillis: Long,
  ): Flow<List<Event>>

  public fun observeAllDayByCalendarAndRange(
    calendarId: String,
    startDate: String,
    endDate: String,
  ): Flow<List<Event>>

  public suspend fun getById(eventId: String): Event?

  public suspend fun upsert(event: Event)

  public suspend fun delete(eventId: String)
}
