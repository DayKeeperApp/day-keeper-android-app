package com.jsamuelsen11.daykeeper.feature.lists.itemedit

data class ItemEditUiState(
  val name: String = "",
  val quantity: String = "1",
  val unit: String = "",
  val isSaving: Boolean = false,
  val nameError: String? = null,
)
