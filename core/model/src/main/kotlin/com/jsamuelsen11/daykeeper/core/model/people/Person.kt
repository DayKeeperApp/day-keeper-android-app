package com.jsamuelsen11.daykeeper.core.model.people

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A person in the user's contacts. */
data class Person(
  val personId: String,
  val spaceId: String,
  val tenantId: String,
  val firstName: String,
  val lastName: String,
  val nickname: String? = null,
  val notes: String? = null,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
