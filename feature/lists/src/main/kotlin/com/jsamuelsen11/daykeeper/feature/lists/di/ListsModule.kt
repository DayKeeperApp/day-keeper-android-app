package com.jsamuelsen11.daykeeper.feature.lists.di

import com.jsamuelsen11.daykeeper.feature.lists.createedit.ListCreateEditViewModel
import com.jsamuelsen11.daykeeper.feature.lists.detail.ShoppingListViewModel
import com.jsamuelsen11.daykeeper.feature.lists.overview.ListsOverviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val listsModule = module {
  viewModel { ListsOverviewViewModel(get(), get(), get()) }
  viewModel { ShoppingListViewModel(get(), get(), get()) }
  viewModel { ListCreateEditViewModel(get(), get()) }
}
