package com.jsamuelsen11.daykeeper

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.intVersion(alias: String): Int =
    findVersion(alias).get().toString().toInt()

internal fun Project.configureAndroidCommon(extension: CommonExtension) {
    val catalog = libs
    extension.apply {
        compileSdk {
            version = release(catalog.intVersion("compile-sdk")) {
                minorApiLevel = catalog.intVersion("compile-sdk-minor")
            }
        }

        defaultConfig.apply {
            minSdk = catalog.intVersion("min-sdk")
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
}
