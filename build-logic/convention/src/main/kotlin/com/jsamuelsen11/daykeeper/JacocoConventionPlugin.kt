package com.jsamuelsen11.daykeeper

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

class JacocoConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("jacoco")

      extensions.configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().toString()
      }

      val isAndroid =
        pluginManager.hasPlugin("com.android.application") ||
          pluginManager.hasPlugin("com.android.library")

      if (isAndroid) {
        configureAndroidCoverage()
      } else {
        configureJvmCoverage()
      }
    }
  }

  private fun Project.configureAndroidCoverage() {
    val exclusions = coverageExclusions()

    tasks.register<JacocoReport>("jacocoTestReport") {
      dependsOn("testDebugUnitTest")

      reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(true)
      }

      sourceDirectories.setFrom(files("src/main/kotlin", "src/main/java"))
      classDirectories.setFrom(
        files(
          fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(exclusions) },
          fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
            exclude(exclusions)
          },
        )
      )
      executionData.setFrom(files(layout.buildDirectory.file("jacoco/testDebugUnitTest.exec")))
    }

    tasks.withType<Test> { finalizedBy("jacocoTestReport") }
  }

  private fun Project.configureJvmCoverage() {
    val exclusions = coverageExclusions()

    tasks.withType<JacocoReport> {
      reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(true)
      }

      classDirectories.setFrom(
        files(classDirectories.files.map { dir -> fileTree(dir) { exclude(exclusions) } })
      )
    }

    tasks.withType<Test> { finalizedBy("jacocoTestReport") }
  }

  private fun coverageExclusions() =
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
}
