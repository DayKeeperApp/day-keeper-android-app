package com.jsamuelsen11.daykeeper

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            val javaTarget = libs.intVersion("java-target")

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.toVersion(javaTarget)
                targetCompatibility = JavaVersion.toVersion(javaTarget)
            }

            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(javaTarget.toString()))
                }
            }
        }
    }
}
