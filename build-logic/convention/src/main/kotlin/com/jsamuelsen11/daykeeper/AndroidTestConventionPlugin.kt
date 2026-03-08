package com.jsamuelsen11.daykeeper

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import de.mannodermaus.gradle.plugins.junit5.AndroidJUnitPlatformPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

class AndroidTestConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply(AndroidJUnitPlatformPlugin::class.java)

      pluginManager.withPlugin("com.android.application") {
        extensions.findByType<ApplicationExtension>()?.testOptions {
          unitTests.isIncludeAndroidResources = true
        }
      }

      pluginManager.withPlugin("com.android.library") {
        extensions.findByType<LibraryExtension>()?.testOptions {
          unitTests.isIncludeAndroidResources = true
        }
      }

      val catalog = libs
      dependencies {
        add("testImplementation", catalog.findBundle("testing-unit").get())
        add("testRuntimeOnly", catalog.findLibrary("junit5-engine").get())
        add("testRuntimeOnly", catalog.findLibrary("junit-platform-launcher").get())
        add("androidTestImplementation", catalog.findBundle("testing-android").get())
      }
    }
  }
}
