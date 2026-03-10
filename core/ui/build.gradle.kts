plugins {
  alias(libs.plugins.daykeeper.android.feature)
  alias(libs.plugins.daykeeper.android.test)
}

android { namespace = "com.jsamuelsen11.daykeeper.core.ui" }

dependencies {
  implementation(project(":core:model"))
  implementation(libs.androidx.compose.material.icons.extended)
}
