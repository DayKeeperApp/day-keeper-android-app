package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.people.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface PersonDao {

  @Query("SELECT * FROM persons WHERE person_id = :personId AND deleted_at IS NULL")
  public fun observeById(personId: String): Flow<PersonEntity?>

  @Query("SELECT * FROM persons WHERE space_id = :spaceId AND deleted_at IS NULL")
  public fun observeBySpace(spaceId: String): Flow<List<PersonEntity>>

  @Query("SELECT * FROM persons WHERE person_id = :personId")
  public suspend fun getById(personId: String): PersonEntity?

  @Upsert public suspend fun upsert(entity: PersonEntity)

  @Upsert public suspend fun upsertAll(entities: List<PersonEntity>)

  @Query(
    "UPDATE persons SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE person_id = :personId"
  )
  public suspend fun softDelete(personId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM persons WHERE person_id = :personId")
  public suspend fun hardDelete(personId: String)
}
