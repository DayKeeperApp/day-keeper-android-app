package com.jsamuelsen11.daykeeper.app.navigation

import kotlinx.serialization.Serializable

// Top-level graph routes (one per bottom nav tab)
@Serializable object CalendarRoute

@Serializable object TasksRoute

@Serializable object ListsRoute

@Serializable object PeopleRoute

@Serializable object ProfileRoute

// Cross-cutting routes
@Serializable object GlobalSearchRoute

