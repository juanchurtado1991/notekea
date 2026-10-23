import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ghost) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.sqldelight) apply false
}

private val gitHooksDirectory = ".githooks"
private val koverPluginId = "org.jetbrains.kotlinx.kover"
private val minimumLineCoveragePercent = 80

/**
 * Coverage rules shared by every module and by the merged root report. Each module must reach the minimum on its
 * own, so a well-tested module cannot hide an untested one. UI is verified visually and by smoke tests instead.
 */
fun KoverProjectExtension.applyNotekeaCoverageRules() {
    reports {
        filters {
            excludes {
                annotatedBy("androidx.compose.runtime.Composable")
                classes(
                    "*ComposableSingletons*",
                    "app.notekea.MainKt"
                )
            }
        }
        verify {
            rule {
                minBound(minimumLineCoveragePercent)
            }
        }
    }
}

dependencies {
    kover(project(":composeApp"))
    kover(project(":ktlint-rules"))
}

kover {
    applyNotekeaCoverageRules()
}

subprojects {
    pluginManager.withPlugin(koverPluginId) {
        extensions.configure<KoverProjectExtension> {
            applyNotekeaCoverageRules()
        }
    }
}

tasks.register<Exec>("installGitHooks") {
    description = "Points git at the versioned hooks in $gitHooksDirectory (pre-commit: ktlintFormat + check)."
    group = "git hooks"
    val isGitRepository = rootDir.resolve(".git").exists()
    onlyIf { isGitRepository }
    commandLine("git", "config", "core.hooksPath", gitHooksDirectory)
}
