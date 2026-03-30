package com.jsamuelsen11.daykeeper.app.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.ktor.client.engine.HttpClientEngine
import org.junit.jupiter.api.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class AppModulesCheckTest {

  @OptIn(KoinExperimentalAPI::class)
  @Test
  fun `all Koin modules resolve without errors`() {
    appModule.verify(
      extraTypes = listOf(Context::class, SavedStateHandle::class, HttpClientEngine::class)
    )
  }
}
