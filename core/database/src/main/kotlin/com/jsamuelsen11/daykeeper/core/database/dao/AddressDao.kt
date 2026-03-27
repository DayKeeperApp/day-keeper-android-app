package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.people.AddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface AddressDao {

  @Query("SELECT * FROM addresses WHERE person_id = :personId AND deleted_at IS NULL")
  public fun observeByPerson(personId: String): Flow<List<AddressEntity>>

  @Query("SELECT * FROM addresses WHERE address_id = :addressId")
  public suspend fun getById(addressId: String): AddressEntity?

  @Upsert public suspend fun upsert(entity: AddressEntity)

  @Upsert public suspend fun upsertAll(entities: List<AddressEntity>)

  @Query(
    "UPDATE addresses SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE address_id = :addressId"
  )
  public suspend fun softDelete(addressId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM addresses WHERE address_id = :addressId")
  public suspend fun hardDelete(addressId: String)

  @Query("SELECT * FROM addresses WHERE updated_at > :since")
  public suspend fun getModifiedSince(since: Long): List<AddressEntity>
}
