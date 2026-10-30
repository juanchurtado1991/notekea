plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

private val jdkVersion = 21

kotlin {
    jvmToolchain(jdkVersion)
    explicitApi()

    compilerOptions {
        allWarningsAsErrors.set(true)
        optIn.add("androidx.compose.ui.test.ExperimentalTestApi")
    }
}

// Test Buddy depends on no Notekea module (qa/SELF_TEST.md), so it can become a standalone SDK.
dependencies {
    api(libs.compose.ui.test)
    api(project(":test-buddy-crash"))
    implementation(compose.desktop.common)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.coroutines.swing)
    compileOnly(libs.junit.jupiter)

    ktlintRuleset(project(":ktlint-rules"))

    testImplementation(compose.desktop.currentOs)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

ktlint {
    version.set(libs.versions.ktlint.cli.get())
    verbose.set(true)
    ignoreFailures.set(false)
}

tasks.test {
    useJUnitPlatform()
}
