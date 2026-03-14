package com.jsamuelsen11.daykeeper.core.data.repository

import com.jsamuelsen11.daykeeper.core.model.people.Address
import kotlinx.coroutines.flow.Flow

public interface AddressRepository {
  public fun observeByPerson(personId: String): Flow<List<Address>>

  public suspend fun getById(addressId: String): Address?

  public suspend fun upsert(address: Address)

  public suspend fun delete(addressId: String)
}
