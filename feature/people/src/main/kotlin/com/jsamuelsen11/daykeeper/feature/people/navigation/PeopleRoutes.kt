package com.jsamuelsen11.daykeeper.feature.people.navigation

import kotlinx.serialization.Serializable

@Serializable object PeopleListRoute

@Serializable data class PersonDetailRoute(val personId: String)

@Serializable data class PersonCreateEditRoute(val personId: String? = null)
