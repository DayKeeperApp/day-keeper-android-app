package com.jsamuelsen11.daykeeper.core.model.people

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A physical address associated with a person. */
data class Address(
  val addressId: String,
  val personId: String,
  val label: String,
  val street: String? = null,
  val city: String? = null,
  val state: String? = null,
  val postalCode: String? = null,
  val country: String? = null,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
