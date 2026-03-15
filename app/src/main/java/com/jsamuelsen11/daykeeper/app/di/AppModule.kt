package com.jsamuelsen11.daykeeper.app.di

import com.jsamuelsen11.daykeeper.core.data.di.dataModule
import com.jsamuelsen11.daykeeper.core.database.di.databaseModule
import com.jsamuelsen11.daykeeper.feature.lists.di.listsModule
import org.koin.dsl.module

val appModule = module { includes(databaseModule, dataModule, listsModule) }
