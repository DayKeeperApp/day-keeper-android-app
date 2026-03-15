package com.jsamuelsen11.daykeeper.app.di

import android.content.Context
import org.junit.jupiter.api.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class AppModulesCheckTest {

  @OptIn(KoinExperimentalAPI::class)
  @Test
  fun `all Koin modules resolve without errors`() {
    appModule.verify(extraTypes = listOf(Context::class))
  }
}
