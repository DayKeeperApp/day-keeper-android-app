package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface EventDao {

  @Query("SELECT * FROM events WHERE event_id = :eventId AND deleted_at IS NULL")
  public fun observeById(eventId: String): Flow<EventEntity?>

  @Query("SELECT * FROM events WHERE calendar_id = :calendarId AND deleted_at IS NULL")
  public fun observeByCalendar(calendarId: String): Flow<List<EventEntity>>

  @Query(
    "SELECT * FROM events WHERE calendar_id = :calendarId" +
      " AND start_at BETWEEN :startMillis AND :endMillis AND deleted_at IS NULL"
  )
  public fun observeByCalendarAndRange(
    calendarId: String,
    startMillis: Long,
    endMillis: Long,
  ): Flow<List<EventEntity>>

  @Query(
    "SELECT * FROM events WHERE calendar_id = :calendarId" +
      " AND start_date BETWEEN :startDate AND :endDate AND deleted_at IS NULL"
  )
  public fun observeAllDayByCalendarAndRange(
    calendarId: String,
    startDate: String,
    endDate: String,
  ): Flow<List<EventEntity>>

  @Query("SELECT * FROM events WHERE event_id = :eventId")
  public suspend fun getById(eventId: String): EventEntity?

  @Upsert public suspend fun upsert(entity: EventEntity)

  @Upsert public suspend fun upsertAll(entities: List<EventEntity>)

  @Query(
    "UPDATE events SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE event_id = :eventId"
  )
  public suspend fun softDelete(eventId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM events WHERE event_id = :eventId")
  public suspend fun hardDelete(eventId: String)
}
