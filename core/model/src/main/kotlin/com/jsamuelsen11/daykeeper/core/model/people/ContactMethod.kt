package com.jsamuelsen11.daykeeper.core.model.people

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A way to contact a person (phone number or email address). */
data class ContactMethod(
  val contactMethodId: String,
  val personId: String,
  val type: ContactMethodType,
  val value: String,
  val label: String,
  val isPrimary: Boolean,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
