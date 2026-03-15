package com.jsamuelsen11.daykeeper.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface AccountDao {

  @Query("SELECT * FROM accounts WHERE tenant_id = :tenantId AND deleted_at IS NULL")
  public fun observeById(tenantId: String): Flow<AccountEntity?>

  @Query("SELECT * FROM accounts WHERE deleted_at IS NULL")
  public fun observeAll(): Flow<List<AccountEntity>>

  @Query("SELECT * FROM accounts WHERE tenant_id = :tenantId")
  public suspend fun getById(tenantId: String): AccountEntity?

  @Upsert public suspend fun upsert(entity: AccountEntity)

  @Upsert public suspend fun upsertAll(entities: List<AccountEntity>)

  @Query(
    "UPDATE accounts SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE tenant_id = :tenantId"
  )
  public suspend fun softDelete(tenantId: String, deletedAt: Long, updatedAt: Long)

  @Query("DELETE FROM accounts WHERE tenant_id = :tenantId")
  public suspend fun hardDelete(tenantId: String)
}
