import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ghost)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

private val appPackageName = "Notekea"
private val appPackageVersion = "1.0.0"
private val jdkVersion = 21
private val mainClassName = "app.notekea.MainKt"
private val nativeAccessJvmArgument = "--enable-native-access=ALL-UNNAMED"

kotlin {
    jvmToolchain(jdkVersion)

    compilerOptions {
        allWarningsAsErrors.set(true)
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.ghost.ktor)
            implementation(libs.jetbrains.markdown)
            implementation(libs.kermit)
            implementation(libs.koin.compose)
            implementation(libs.koin.core)
            implementation(libs.koog.agents.core)
            implementation(libs.koog.agents.tools)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.markdown.renderer.m3)
            implementation(libs.multiplatform.settings)
            implementation(libs.okio)
            implementation(libs.sqldelight.runtime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(libs.directory.watcher)
            implementation(libs.java.keyring)
            implementation(libs.jgit)
            implementation(libs.jgit.ssh.apache)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.llama)
            implementation(libs.lucene.core)
            implementation(libs.sqldelight.sqlite.driver)
        }

        jvmTest.dependencies {
            implementation(libs.konsist)
        }
    }
}

dependencies {
    ktlintRuleset(project(":ktlint-rules"))
}

sqldelight {
    databases {
        create("NotekeaDatabase") {
            packageName.set("app.notekea.db")
        }
    }
}

ktlint {
    version.set(libs.versions.ktlint.cli.get())
    verbose.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
        exclude { element ->
            element.file.path.contains("/build/generated/")
        }
    }
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
    workerMaxHeapSize.set("1024m")
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask>().configureEach {
    dependsOn(tasks.matching { it.name.startsWith("ksp") })
    if (name.contains("KotlinScripts")) {
        enabled = false
    }
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask>().configureEach {
    dependsOn(tasks.matching { it.name.startsWith("ksp") })
    if (name.contains("KotlinScripts")) {
        enabled = false
    }
}

// Every developer who compiles the app gets the pre-commit hook installed, so formatting is never skipped.
tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
    dependsOn(":installGitHooks")
}

compose.resources {
    packageOfResClass = "app.notekea.resources"
    publicResClass = false
}

compose.desktop {
    application {
        mainClass = mainClassName
        // Skiko and java-llama.cpp load native libraries; without this the JVM warns on every launch.
        jvmArgs += nativeAccessJvmArgument

        nativeDistributions {
            targetFormats(
                TargetFormat.Deb,
                TargetFormat.Dmg,
                TargetFormat.Msi
            )
            packageName = appPackageName
            packageVersion = appPackageVersion
        }
    }
}
