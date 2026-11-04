plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("notekea.jvm")
    alias(libs.plugins.kover)
}

kotlin {
    explicitApi()

    compilerOptions {
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

    testImplementation(compose.desktop.currentOs)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
