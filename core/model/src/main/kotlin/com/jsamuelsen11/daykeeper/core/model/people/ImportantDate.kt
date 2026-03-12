package com.jsamuelsen11.daykeeper.core.model.people

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel

/** A notable date associated with a person (e.g. birthday, anniversary). */
data class ImportantDate(
  val importantDateId: String,
  val personId: String,
  val label: String,
  val date: String,
  val createdAt: Long,
  val updatedAt: Long,
  val deletedAt: Long? = null,
) : DayKeeperModel
