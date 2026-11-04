import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

// What every Notekea module shares: the JDK, warnings as errors, ktlint with the project's own rules and the Compose
// stability configuration.
plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

private val ktlintRulesPath = ":ktlint-rules"

listOf("org.jetbrains.kotlin.jvm", "org.jetbrains.kotlin.multiplatform").forEach { kotlinPlugin ->
    pluginManager.withPlugin(kotlinPlugin) {
        extensions.getByType<KotlinTopLevelExtension>().jvmToolchain(NotekeaVersions.jdk(project = project))
    }
}

pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
    extensions.configure<ComposeCompilerGradlePluginExtension> {
        stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose-stability.conf"))
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.allWarningsAsErrors.set(true)
}

ktlint {
    version.set(NotekeaVersions.ktlint(project = project))
    verbose.set(true)
    ignoreFailures.set(false)
}

if (path != ktlintRulesPath) {
    dependencies {
        ktlintRuleset(project(ktlintRulesPath))
    }
}
