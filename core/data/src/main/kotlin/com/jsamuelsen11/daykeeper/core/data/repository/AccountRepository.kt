package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.account.Account
import kotlinx.coroutines.flow.Flow

public interface AccountRepository {
  public fun observeById(tenantId: String): Flow<Account?>

  public fun observeAll(): Flow<List<Account>>

  public suspend fun getById(tenantId: String): Account?

  public suspend fun upsert(account: Account)

  public suspend fun delete(tenantId: String)
}
