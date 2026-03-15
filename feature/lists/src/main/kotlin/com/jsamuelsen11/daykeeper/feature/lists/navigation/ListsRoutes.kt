package com.jsamuelsen11.daykeeper.feature.lists.navigation

import kotlinx.serialization.Serializable

@Serializable object ListsHomeRoute

@Serializable data class ListDetailRoute(val listId: String)

@Serializable data class ListCreateEditRoute(val listId: String? = null)
