plugins { `kotlin-dsl` }

dependencies {
  compileOnly(libs.android.gradle.plugin)
  compileOnly(libs.kotlin.gradle.plugin)
  compileOnly(libs.compose.gradle.plugin)
  implementation(libs.android.junit5.gradle.plugin)
}

gradlePlugin {
  plugins {
    register("androidApplication") {
      id = "daykeeper.android.application"
      implementationClass = "com.jsamuelsen11.daykeeper.AndroidApplicationConventionPlugin"
    }
    register("androidLibrary") {
      id = "daykeeper.android.library"
      implementationClass = "com.jsamuelsen11.daykeeper.AndroidLibraryConventionPlugin"
    }
    register("androidFeature") {
      id = "daykeeper.android.feature"
      implementationClass = "com.jsamuelsen11.daykeeper.AndroidFeatureConventionPlugin"
    }
    register("kotlinLibrary") {
      id = "daykeeper.kotlin.library"
      implementationClass = "com.jsamuelsen11.daykeeper.KotlinLibraryConventionPlugin"
    }
    register("androidTest") {
      id = "daykeeper.android.test"
      implementationClass = "com.jsamuelsen11.daykeeper.AndroidTestConventionPlugin"
    }
    register("jacocoCoverage") {
      id = "daykeeper.jacoco"
      implementationClass = "com.jsamuelsen11.daykeeper.JacocoConventionPlugin"
    }
  }
}
