plugins {
  alias(libs.plugins.daykeeper.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.google.services) apply false
}

if (file("google-services.json").exists()) {
  apply(plugin = libs.plugins.google.services.get().pluginId)
}

android {
  namespace = "com.jsamuelsen11.daykeeper.app"

  defaultConfig {
    applicationId = "com.jsamuelsen11.daykeeper.app"
    versionCode = 3
    versionName = "1.0.2"
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  buildTypes {
    debug { buildConfigField("String", "SYNC_BASE_URL", "\"https://api.daykeeper.local/\"") }
    release { buildConfigField("String", "SYNC_BASE_URL", "\"https://api.daykeeper.local/\"") }
  }
}

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:common"))
  implementation(project(":core:ui"))
  implementation(project(":core:database"))
  implementation(project(":core:data"))
  implementation(project(":core:network"))

  implementation(project(":feature:calendar"))
  implementation(project(":feature:lists"))
  implementation(project(":feature:people"))
  implementation(project(":feature:tasks"))
  implementation(project(":feature:profile"))

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.bundles.compose)
  implementation(libs.bundles.lifecycle)

  implementation(libs.androidx.navigation.compose)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.androidx.work.runtime.ktx)

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.messaging)

  implementation(platform(libs.koin.bom))
  implementation(libs.koin.core)
  implementation(libs.koin.android)
  implementation(libs.koin.androidx.workmanager)

  testImplementation(platform(libs.koin.bom))
  testImplementation(libs.koin.test)
  testImplementation(libs.koin.test.junit5)
  testImplementation(libs.ktor.client.core)

  androidTestImplementation(platform(libs.androidx.compose.bom))
  debugImplementation(libs.bundles.compose.debug)
}
