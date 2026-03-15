package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.AccountDao
import com.jsamuelsen11.daykeeper.core.model.account.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class AccountRepositoryImpl(private val dao: AccountDao) : AccountRepository {
  public override fun observeById(tenantId: String): Flow<Account?> =
    dao.observeById(tenantId).map { it?.toDomain() }

  public override fun observeAll(): Flow<List<Account>> =
    dao.observeAll().map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(tenantId: String): Account? =
    dao.getById(tenantId)?.toDomain()

  public override suspend fun upsert(account: Account) {
    dao.upsert(account.toEntity())
  }

  public override suspend fun delete(tenantId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(tenantId, deletedAt = now, updatedAt = now)
  }
}
