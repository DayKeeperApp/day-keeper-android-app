plugins {
  alias(libs.plugins.daykeeper.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.jsamuelsen11.daykeeper.app"

  defaultConfig {
    applicationId = "com.jsamuelsen11.daykeeper.app"
    versionCode = 3
    versionName = "1.0.2"
  }

  buildFeatures { compose = true }
}

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:common"))
  implementation(project(":core:ui"))
  implementation(project(":core:database"))
  implementation(project(":core:data"))

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.bundles.compose)
  implementation(libs.bundles.lifecycle)

  implementation(libs.androidx.navigation.compose)
  implementation(libs.kotlinx.serialization.json)

  implementation(platform(libs.koin.bom))
  implementation(libs.koin.core)
  implementation(libs.koin.android)

  testImplementation(platform(libs.koin.bom))
  testImplementation(libs.koin.test)
  testImplementation(libs.koin.test.junit5)

  androidTestImplementation(platform(libs.androidx.compose.bom))
  debugImplementation(libs.bundles.compose.debug)
}
