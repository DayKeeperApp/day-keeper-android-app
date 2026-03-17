package com.jsamuelsen11.daykeeper.feature.tasks.di

import com.jsamuelsen11.daykeeper.feature.tasks.category.CategoryManagementViewModel
import com.jsamuelsen11.daykeeper.feature.tasks.createedit.TaskCreateEditViewModel
import com.jsamuelsen11.daykeeper.feature.tasks.detail.TaskDetailViewModel
import com.jsamuelsen11.daykeeper.feature.tasks.list.TaskListViewModel
import com.jsamuelsen11.daykeeper.feature.tasks.project.createedit.ProjectCreateEditViewModel
import com.jsamuelsen11.daykeeper.feature.tasks.project.detail.ProjectDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val tasksModule = module {
  viewModelOf(::TaskListViewModel)
  viewModelOf(::TaskDetailViewModel)
  viewModelOf(::TaskCreateEditViewModel)
  viewModelOf(::ProjectDetailViewModel)
  viewModelOf(::ProjectCreateEditViewModel)
  viewModelOf(::CategoryManagementViewModel)
}
