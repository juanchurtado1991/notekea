plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

/** The artifact of a plugin of the catalog, for the convention plugin's code to compile against. */
fun marker(
    plugin: Provider<PluginDependency>
) = plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

dependencies {
    // In one class loader with Kotlin and Compose, which look for the Android plugin's classes.
    implementation(marker(libs.plugins.android.kotlin.multiplatform.library))
    implementation(marker(libs.plugins.compose.compiler))
    implementation(marker(libs.plugins.compose.multiplatform))
    implementation(marker(libs.plugins.kotlin.jvm))
    implementation(marker(libs.plugins.ktlint))

    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

// The custom rules of ktlint-rules/ are built by the main build, so they cannot reach the code that builds it.
ktlint {
    version.set(libs.versions.ktlint.cli.get())
    verbose.set(true)
    ignoreFailures.set(false)

    // The accessors Gradle generates for the convention plugins are not ours to lay out.
    filter {
        exclude { it.file.path.contains("generated-sources") }
    }
}
