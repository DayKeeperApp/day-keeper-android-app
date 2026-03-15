package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface EventReminderDao {

  @Query("SELECT * FROM event_reminders WHERE event_id = :eventId AND deleted_at IS NULL")
  public fun observeByEvent(eventId: String): Flow<List<EventReminderEntity>>

  @Query("SELECT * FROM event_reminders WHERE reminder_id = :reminderId")
  public suspend fun getById(reminderId: String): EventReminderEntity?

  @Upsert public suspend fun upsert(entity: EventReminderEntity)

  @Upsert public suspend fun upsertAll(entities: List<EventReminderEntity>)

  @Query(
    "UPDATE event_reminders SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE reminder_id = :reminderId"
  )
  public suspend fun softDelete(reminderId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM event_reminders WHERE reminder_id = :reminderId")
  public suspend fun hardDelete(reminderId: String)
}
