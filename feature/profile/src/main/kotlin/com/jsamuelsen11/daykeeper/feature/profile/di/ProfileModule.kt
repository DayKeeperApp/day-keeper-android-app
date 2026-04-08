package com.jsamuelsen11.daykeeper.feature.profile.di

import com.jsamuelsen11.daykeeper.feature.profile.overview.ProfileOverviewViewModel
import com.jsamuelsen11.daykeeper.feature.profile.settings.AccountSettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
  viewModelOf(::ProfileOverviewViewModel)
  viewModelOf(::AccountSettingsViewModel)
}
