package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.data.mapper.toDomain
import com.jsamuelsen11.daykeeper.core.data.mapper.toEntity
import com.jsamuelsen11.daykeeper.core.database.dao.AddressDao
import com.jsamuelsen11.daykeeper.core.model.people.Address
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class AddressRepositoryImpl(private val dao: AddressDao) : AddressRepository {
  public override fun observeByPerson(personId: String): Flow<List<Address>> =
    dao.observeByPerson(personId).map { list -> list.map { it.toDomain() } }

  public override suspend fun getById(addressId: String): Address? =
    dao.getById(addressId)?.toDomain()

  public override suspend fun upsert(address: Address) {
    dao.upsert(address.toEntity())
  }

  public override suspend fun delete(addressId: String) {
    val now = System.currentTimeMillis()
    dao.softDelete(addressId, deletedAt = now, updatedAt = now)
  }
}
