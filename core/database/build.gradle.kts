plugins {
  alias(libs.plugins.daykeeper.android.library)
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
}

android { namespace = "com.jsamuelsen11.daykeeper.core.database" }

room { schemaDirectory("$projectDir/schemas") }

dependencies {
  implementation(project(":core:model"))

  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)

  implementation(platform(libs.koin.bom))
  implementation(libs.koin.core)
  implementation(libs.koin.android)

  testImplementation(libs.androidx.room.testing)
}
