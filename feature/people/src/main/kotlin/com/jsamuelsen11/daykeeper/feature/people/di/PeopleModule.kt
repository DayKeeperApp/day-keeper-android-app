package com.jsamuelsen11.daykeeper.feature.people.di

import com.jsamuelsen11.daykeeper.feature.people.createedit.PersonCreateEditViewModel
import com.jsamuelsen11.daykeeper.feature.people.detail.PersonDetailViewModel
import com.jsamuelsen11.daykeeper.feature.people.list.PeopleListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val peopleModule = module {
  viewModel { PeopleListViewModel(get(), get()) }
  viewModel { PersonDetailViewModel(get(), get(), get(), get(), get()) }
  viewModel { PersonCreateEditViewModel(get(), get(), get(), get(), get()) }
}
