plugins { alias(libs.plugins.daykeeper.android.feature) }

android { namespace = "com.jsamuelsen11.daykeeper.core.ui" }

dependencies { implementation(project(":core:model")) }
