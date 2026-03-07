package com.jsamuelsen11.daykeeper

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.android.application")

      extensions.configure<ApplicationExtension> {
        configureAndroidCommon(this)

        defaultConfig.apply { targetSdk = libs.intVersion("target-sdk") }

        val javaTarget = libs.intVersion("java-target")
        compileOptions {
          sourceCompatibility = JavaVersion.toVersion(javaTarget)
          targetCompatibility = JavaVersion.toVersion(javaTarget)
        }

        buildTypes {
          release {
            isMinifyEnabled = false
            proguardFiles(
              getDefaultProguardFile("proguard-android-optimize.txt"),
              "proguard-rules.pro",
            )
          }
        }
      }
    }
  }
}
