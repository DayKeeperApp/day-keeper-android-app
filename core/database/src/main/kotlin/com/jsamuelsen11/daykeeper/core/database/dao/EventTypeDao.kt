package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.calendar.EventTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface EventTypeDao {

  @Query("SELECT * FROM event_types") public fun observeAll(): Flow<List<EventTypeEntity>>

  @Query("SELECT * FROM event_types WHERE event_type_id = :eventTypeId")
  public suspend fun getById(eventTypeId: String): EventTypeEntity?

  @Upsert public suspend fun upsert(entity: EventTypeEntity)

  @Upsert public suspend fun upsertAll(entities: List<EventTypeEntity>)

  @Query("DELETE FROM event_types WHERE event_type_id = :eventTypeId")
  public suspend fun hardDelete(eventTypeId: String)
}
