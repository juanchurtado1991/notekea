import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

private val jdkVersion = 21

kotlin {
    jvmToolchain(jdkVersion)

    compilerOptions {
        allWarningsAsErrors.set(true)
        // The rule set runs on ktlint's own Kotlin stdlib (2.2), so newer stdlib APIs must not be used.
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
    }
}

dependencies {
    compileOnly(libs.ktlint.cli.ruleset.core)
    compileOnly(libs.ktlint.rule.engine.core)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.ktlint.cli.ruleset.core)
    testImplementation(libs.ktlint.test)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.slf4j.simple)
}

ktlint {
    version.set(
        libs.versions.ktlint.cli
            .get()
    )
    verbose.set(true)
    ignoreFailures.set(false)
}

tasks.test {
    useJUnitPlatform()
}
