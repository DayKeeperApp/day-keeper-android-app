package com.jsamuelsen11.daykeeper.feature.tasks.navigation

import kotlinx.serialization.Serializable

@Serializable object TasksHomeRoute

@Serializable data class TaskDetailRoute(val taskId: String)

@Serializable
data class TaskCreateEditRoute(val taskId: String? = null, val projectId: String? = null)

@Serializable data class ProjectDetailRoute(val projectId: String)

@Serializable data class ProjectCreateEditRoute(val projectId: String? = null)

@Serializable object CategoryManagementRoute
