plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("notekea.jvm")
    alias(libs.plugins.kover)
}

kotlin { explicitApi() }

// Test Buddy Crash ships in the app's release (test-buddy-crash/README.md): it depends on no Notekea module and on no
// test library, only on Compose for its dialog.
dependencies {
    implementation(compose.desktop.common)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.coroutines.swing)

    testImplementation(compose.desktop.currentOs)
    testImplementation(libs.compose.ui.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test { useJUnitPlatform() }
