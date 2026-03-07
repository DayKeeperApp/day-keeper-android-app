package com.jsamuelsen11.daykeeper

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("daykeeper.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                val catalog = libs
                add("implementation", platform(catalog.findLibrary("androidx-compose-bom").get()))
                add("implementation", catalog.findBundle("compose").get())
                add("implementation", catalog.findLibrary("androidx-navigation-compose").get())
                add("implementation", catalog.findBundle("lifecycle").get())
                add("debugImplementation", catalog.findBundle("compose-debug").get())
            }
        }
    }
}
