plugins {
  alias(libs.plugins.daykeeper.android.application)
  alias(libs.plugins.kotlin.compose)
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

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.bundles.compose)
  implementation(libs.bundles.lifecycle)

  androidTestImplementation(platform(libs.androidx.compose.bom))
  debugImplementation(libs.bundles.compose.debug)
}
