package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.calendar.EventReminder
import kotlinx.coroutines.flow.Flow

public interface EventReminderRepository {
  public fun observeByEvent(eventId: String): Flow<List<EventReminder>>

  public suspend fun getById(reminderId: String): EventReminder?

  public suspend fun upsert(reminder: EventReminder)

  public suspend fun delete(reminderId: String)

  public suspend fun getAllActive(): List<EventReminder>
}
