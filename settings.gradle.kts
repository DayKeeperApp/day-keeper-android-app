pluginManagement {
  includeBuild("build-logic")
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("com.gradle.develocity") version "4.3.2"
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

develocity {
  buildScan {
    termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
    termsOfUseAgree.set("yes")
    publishing.onlyIf { System.getenv("CI") != null }
    uploadInBackground.set(System.getenv("CI") == null)
  }
}

rootProject.name = "Day Keeper"

include(":app")

include(":core:model")

include(":core:common")

include(":core:database")

include(":core:data")

include(":core:ui")

include(":feature:lists")

include(":feature:people")

include(":feature:tasks")

include(":feature:calendar")
