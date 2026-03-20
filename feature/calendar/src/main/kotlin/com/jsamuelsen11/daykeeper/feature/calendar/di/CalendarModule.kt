package com.jsamuelsen11.daykeeper.feature.calendar.di

import com.jsamuelsen11.daykeeper.feature.calendar.createedit.EventCreateEditViewModel
import com.jsamuelsen11.daykeeper.feature.calendar.detail.EventDetailViewModel
import com.jsamuelsen11.daykeeper.feature.calendar.home.CalendarViewModel
import com.jsamuelsen11.daykeeper.feature.calendar.management.CalendarCreateEditViewModel
import com.jsamuelsen11.daykeeper.feature.calendar.management.CalendarManagementViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val calendarModule = module {
  viewModelOf(::CalendarViewModel)
  viewModelOf(::EventDetailViewModel)
  viewModelOf(::EventCreateEditViewModel)
  viewModelOf(::CalendarManagementViewModel)
  viewModelOf(::CalendarCreateEditViewModel)
}
