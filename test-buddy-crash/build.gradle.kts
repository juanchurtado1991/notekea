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
    }
}

// Test Buddy Crash ships in the app's release (test-buddy-crash/README.md): it depends on no Notekea module and on no
// test library, only on Compose for its dialog.
dependencies {
    implementation(compose.desktop.common)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.coroutines.swing)

    ktlintRuleset(project(":ktlint-rules"))

    testImplementation(compose.desktop.currentOs)
    testImplementation(libs.compose.ui.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)
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
