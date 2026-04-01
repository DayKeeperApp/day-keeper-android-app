plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
  alias(libs.plugins.spotless)
  jacoco
}

detekt {
  parallel = true
  buildUponDefaultConfig = true
  config.setFrom("config/detekt/detekt.yml")
  basePath = rootProject.projectDir.absolutePath
}

subprojects {
  apply(plugin = "org.jetbrains.dokka")
  apply(plugin = "io.gitlab.arturbosch.detekt")

  detekt {
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    basePath = rootProject.projectDir.absolutePath
  }
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

val coverageExclusions =
  listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*ComposableSingletons*.*",
    "**/*_ModuleKt*.*",
    "**/*_Impl.*",
    "**/*_Impl$*.*",
  )

tasks.register<JacocoReport>("jacocoMergedReport") {
  group = "verification"
  description = "Generates merged code coverage report for all modules."

  dependsOn(
    subprojects.flatMap {
      it.tasks.matching { t -> t.name == "test" || t.name == "testDebugUnitTest" }
    }
  )

  val sourceDirectories =
    files(
      subprojects.map { it.file("src/main/kotlin") } + subprojects.map { it.file("src/main/java") }
    )
  val classDirectoryFiles =
    files(
      subprojects.map { sub ->
        fileTree(sub.layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
          exclude(coverageExclusions)
        }
      } +
        subprojects.map { sub ->
          fileTree(sub.layout.buildDirectory.dir("classes/kotlin/main")) {
            exclude(coverageExclusions)
          }
        } +
        subprojects.map { sub ->
          fileTree(sub.layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
            exclude(coverageExclusions)
          }
        } +
        subprojects.map { sub ->
          fileTree(sub.layout.buildDirectory.dir("classes/java/main")) {
            exclude(coverageExclusions)
          }
        }
    )
  val executionDataFiles =
    files(
        subprojects.flatMap { sub ->
          listOf(
            sub.layout.buildDirectory.file("jacoco/testDebugUnitTest.exec"),
            sub.layout.buildDirectory.file("jacoco/test.exec"),
          )
        }
      )
      .filter { it.exists() }

  sourceDirectories.setFrom(sourceDirectories)
  classDirectories.setFrom(classDirectoryFiles)
  executionData.setFrom(executionDataFiles)

  reports {
    html.required.set(true)
    html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/merged/html"))
    xml.required.set(true)
    xml.outputLocation.set(
      layout.buildDirectory.file("reports/jacoco/merged/jacocoMergedReport.xml")
    )
    csv.required.set(true)
    csv.outputLocation.set(
      layout.buildDirectory.file("reports/jacoco/merged/jacocoMergedReport.csv")
    )
  }
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
  group = "verification"
  description = "Verifies code coverage meets minimum threshold."

  dependsOn("jacocoMergedReport")

  val mergedReport = tasks.named<JacocoReport>("jacocoMergedReport")
  classDirectories.setFrom(mergedReport.map { it.classDirectories })
  executionData.setFrom(mergedReport.map { it.executionData })
  sourceDirectories.setFrom(mergedReport.map { it.sourceDirectories })

  violationRules {
    rule {
      limit {
        minimum =
          project.findProperty("jacoco.minimumCoverage")?.toString()?.toBigDecimal()
            ?: "0.80".toBigDecimal()
      }
    }
  }
}
