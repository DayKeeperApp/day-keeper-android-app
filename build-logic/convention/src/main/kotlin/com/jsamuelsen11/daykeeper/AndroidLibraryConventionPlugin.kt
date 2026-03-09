package com.jsamuelsen11.daykeeper

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.android.library")
      pluginManager.apply("daykeeper.android.test")

      extensions.configure<LibraryExtension> {
        configureAndroidCommon(this)

        val javaTarget = libs.intVersion("java-target")
        compileOptions {
          sourceCompatibility = JavaVersion.toVersion(javaTarget)
          targetCompatibility = JavaVersion.toVersion(javaTarget)
        }

        defaultConfig.apply { consumerProguardFiles("consumer-rules.pro") }

        lint {
          warningsAsErrors = true
          abortOnError = true
          checkDependencies = true
          baseline = project.file("lint-baseline.xml")
          disable += "NewerVersionAvailable"
          disable += "GradleDependency"
        }
      }
    }
  }
}
