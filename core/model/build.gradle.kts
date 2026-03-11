import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { alias(libs.plugins.daykeeper.kotlin.library) }

private val testJavaVersion = 17

kotlin { compilerOptions { freeCompilerArgs.add("-Xjdk-release=11") } }

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileTestKotlin") {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(testJavaVersion.toString()))
    freeCompilerArgs.set(listOf("-Xjdk-release=$testJavaVersion"))
  }
}

tasks.named<JavaCompile>("compileTestJava") {
  sourceCompatibility = testJavaVersion.toString()
  targetCompatibility = testJavaVersion.toString()
}

tasks.withType<Test> {
  useJUnitPlatform()
  javaLauncher.set(
    javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(testJavaVersion)) }
  )
}

dependencies {
  testImplementation(libs.bundles.testing.unit)
  testRuntimeOnly(libs.junit5.engine)
  testRuntimeOnly(libs.junit.platform.launcher)
}
