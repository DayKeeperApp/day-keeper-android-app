package com.jsamuelsen11.daykeeper.core.network.di

import com.jsamuelsen11.daykeeper.core.network.api.SyncApi
import com.jsamuelsen11.daykeeper.core.network.api.SyncApiImpl
import com.jsamuelsen11.daykeeper.core.network.auth.EncryptedTokenStore
import com.jsamuelsen11.daykeeper.core.network.auth.TokenStore
import com.jsamuelsen11.daykeeper.core.network.client.HttpClientFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
  single<TokenStore> { EncryptedTokenStore(androidContext()) }
  single { HttpClientFactory.create(config = get(), tokenStore = get()) }
  single { SyncApiImpl(get()) } bind SyncApi::class
}
