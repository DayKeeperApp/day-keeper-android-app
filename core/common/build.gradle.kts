plugins { alias(libs.plugins.daykeeper.android.library) }

android { namespace = "com.jsamuelsen11.daykeeper.core.common" }

dependencies { implementation(project(":core:model")) }
