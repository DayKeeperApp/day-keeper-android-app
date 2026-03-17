package com.jsamuelsen11.daykeeper.feature.tasks.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.jsamuelsen11.daykeeper.feature.tasks.category.CategoryManagementScreen
import com.jsamuelsen11.daykeeper.feature.tasks.createedit.TaskCreateEditScreen
import com.jsamuelsen11.daykeeper.feature.tasks.detail.TaskDetailScreen
import com.jsamuelsen11.daykeeper.feature.tasks.list.TaskListScreen
import com.jsamuelsen11.daykeeper.feature.tasks.project.createedit.ProjectCreateEditScreen
import com.jsamuelsen11.daykeeper.feature.tasks.project.detail.ProjectDetailScreen

fun NavGraphBuilder.tasksGraph(navController: NavHostController) {
  composable<TasksHomeRoute> {
    TaskListScreen(
      onTaskClick = { taskId -> navController.navigate(TaskDetailRoute(taskId)) },
      onCreateTask = { navController.navigate(TaskCreateEditRoute()) },
      onCreateProject = { navController.navigate(ProjectCreateEditRoute()) },
      onProjectClick = { projectId -> navController.navigate(ProjectDetailRoute(projectId)) },
    )
  }
  composable<TaskDetailRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<TaskDetailRoute>()
    TaskDetailScreen(
      onNavigateBack = { navController.popBackStack() },
      onEditTask = { navController.navigate(TaskCreateEditRoute(taskId = route.taskId)) },
      onProjectClick = { projectId -> navController.navigate(ProjectDetailRoute(projectId)) },
    )
  }
  composable<TaskCreateEditRoute> {
    TaskCreateEditScreen(
      onNavigateBack = { navController.popBackStack() },
      onManageCategories = { navController.navigate(CategoryManagementRoute) },
    )
  }
  composable<ProjectDetailRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<ProjectDetailRoute>()
    ProjectDetailScreen(
      onNavigateBack = { navController.popBackStack() },
      onEditProject = { navController.navigate(ProjectCreateEditRoute(route.projectId)) },
      onTaskClick = { taskId -> navController.navigate(TaskDetailRoute(taskId)) },
      onCreateTask = { navController.navigate(TaskCreateEditRoute(projectId = route.projectId)) },
    )
  }
  composable<ProjectCreateEditRoute> {
    ProjectCreateEditScreen(onNavigateBack = { navController.popBackStack() })
  }
  composable<CategoryManagementRoute> {
    CategoryManagementScreen(onNavigateBack = { navController.popBackStack() })
  }
}
