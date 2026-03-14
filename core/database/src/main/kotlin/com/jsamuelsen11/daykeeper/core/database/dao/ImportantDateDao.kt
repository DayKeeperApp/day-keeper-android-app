package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.people.ImportantDateEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface ImportantDateDao {

  @Query("SELECT * FROM important_dates WHERE person_id = :personId AND deleted_at IS NULL")
  public fun observeByPerson(personId: String): Flow<List<ImportantDateEntity>>

  @Query("SELECT * FROM important_dates WHERE important_date_id = :importantDateId")
  public suspend fun getById(importantDateId: String): ImportantDateEntity?

  @Upsert public suspend fun upsert(entity: ImportantDateEntity)

  @Upsert public suspend fun upsertAll(entities: List<ImportantDateEntity>)

  @Query(
    "UPDATE important_dates SET deleted_at = :deletedAt, updated_at = :updatedAt" +
      " WHERE important_date_id = :importantDateId"
  )
  public suspend fun softDelete(importantDateId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM important_dates WHERE important_date_id = :importantDateId")
  public suspend fun hardDelete(importantDateId: String)
}
