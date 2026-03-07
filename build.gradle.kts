plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.spotless)
}

spotless {
  kotlin {
    target("**/*.kt")
    targetExclude("**/build/**")
    ktfmt(libs.versions.ktfmt.get()).googleStyle()
  }

  kotlinGradle {
    target("**/*.kts")
    targetExclude("**/build/**")
    ktfmt(libs.versions.ktfmt.get()).googleStyle()
  }

  format("xml") {
    target("**/*.xml")
    targetExclude("**/build/**", ".idea/**")
    leadingTabsToSpaces(4)
    trimTrailingWhitespace()
    endWithNewline()
  }

  json {
    target("**/*.json")
    targetExclude("**/build/**", ".idea/**", ".beads/**")
    gson().indentWithSpaces(2).sortByKeys()
  }
}
