package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.CalendarEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface CalendarDao {

  @Query("SELECT * FROM calendars WHERE calendar_id = :calendarId AND deleted_at IS NULL")
  public fun observeById(calendarId: String): Flow<CalendarEntity?>

  @Query("SELECT * FROM calendars WHERE space_id = :spaceId AND deleted_at IS NULL")
  public fun observeBySpace(spaceId: String): Flow<List<CalendarEntity>>

  @Query("SELECT * FROM calendars WHERE calendar_id = :calendarId")
  public suspend fun getById(calendarId: String): CalendarEntity?

  @Upsert public suspend fun upsert(entity: CalendarEntity)

  @Upsert public suspend fun upsertAll(entities: List<CalendarEntity>)

  @Query(
    "UPDATE calendars SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE calendar_id = :calendarId"
  )
  public suspend fun softDelete(calendarId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM calendars WHERE calendar_id = :calendarId")
  public suspend fun hardDelete(calendarId: String)

  @Query("SELECT * FROM calendars WHERE updated_at > :since")
  public suspend fun getModifiedSince(since: Long): List<CalendarEntity>
}
