package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.people.ContactMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface ContactMethodDao {

  @Query("SELECT * FROM contact_methods WHERE person_id = :personId AND deleted_at IS NULL")
  public fun observeByPerson(personId: String): Flow<List<ContactMethodEntity>>

  @Query("SELECT * FROM contact_methods WHERE contact_method_id = :contactMethodId")
  public suspend fun getById(contactMethodId: String): ContactMethodEntity?

  @Upsert public suspend fun upsert(entity: ContactMethodEntity)

  @Upsert public suspend fun upsertAll(entities: List<ContactMethodEntity>)

  @Query(
    "UPDATE contact_methods SET deleted_at = :deletedAt, updated_at = :updatedAt" +
      " WHERE contact_method_id = :contactMethodId"
  )
  public suspend fun softDelete(contactMethodId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM contact_methods WHERE contact_method_id = :contactMethodId")
  public suspend fun hardDelete(contactMethodId: String)

  @Query("SELECT * FROM contact_methods WHERE updated_at > :since")
  public suspend fun getModifiedSince(since: Long): List<ContactMethodEntity>
}
