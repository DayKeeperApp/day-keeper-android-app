package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.calendar.Calendar
import kotlinx.coroutines.flow.Flow

public interface CalendarRepository {
  public fun observeById(calendarId: String): Flow<Calendar?>

  public fun observeBySpace(spaceId: String): Flow<List<Calendar>>

  public suspend fun getById(calendarId: String): Calendar?

  public suspend fun upsert(calendar: Calendar)

  public suspend fun delete(calendarId: String)
}
