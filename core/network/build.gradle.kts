plugins {
  alias(libs.plugins.daykeeper.android.library)
  alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.jsamuelsen11.daykeeper.core.network" }

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:database"))

  implementation(libs.bundles.ktor.client)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.androidx.security.crypto)

  implementation(platform(libs.koin.bom))
  implementation(libs.koin.core)
  implementation(libs.koin.android)

  testImplementation(libs.ktor.client.mock)
}
