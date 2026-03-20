plugins {
  alias(libs.plugins.daykeeper.android.feature)
  alias(libs.plugins.daykeeper.android.test)
  alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.jsamuelsen11.daykeeper.feature.calendar" }

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:data"))
  implementation(project(":core:ui"))

  implementation(platform(libs.koin.bom))
  implementation(libs.koin.core)
  implementation(libs.koin.android)
  implementation(libs.koin.compose)
  implementation(libs.koin.compose.viewmodel)

  implementation(libs.kotlinx.serialization.json)
}
