import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ghost)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

private val appPackageName = "Notekea"
// The installers need a plain version whose first number is above 0 (DMG) and with no suffix (MSI); people see
// appDisplayVersion, in About and crash reports, until the first public release.
private val appPackageVersion = "1.0.0"
private val appDisplayVersion = "0.1.0-alpha"
private val appVersionSystemProperty = "notekea.version"
private val jdkVersion = 21
private val androidCompileSdk = 36
private val androidMinSdk = 26
private val androidNamespace = "app.notekea"
private val galleryGradleProperty = "gallery"
private val gallerySystemProperty = "notekea.gallery"
private val mainClassName = "app.notekea.MainKt"
private val appBundleId = "app.notekea"
private val appDescription = "Local-first notes, compatible with Obsidian vaults"
private val appVendor = "Notekea"
private val linuxAppCategory = "Office"
// Never change it: Windows recognizes a newer installer as an update of the installed app by this ID.
private val windowsUpgradeUuid = "8a661b42-78a2-4f14-83a6-1249b614b495"
private val nativeAccessJvmArgument = "--enable-native-access=ALL-UNNAMED"

// The JDK modules the installers keep, shared with the Notekea Self-Test (gradle.properties).
private val runtimeModules = providers.gradleProperty("notekea.runtimeModules").get().split(",")

kotlin {
    jvmToolchain(jdkVersion)

    compilerOptions {
        allWarningsAsErrors.set(true)
    }

    jvm()

    // Android target only so Android Studio can render the common @Previews (no Android app yet).
    android {
        namespace = androidNamespace
        compileSdk = androidCompileSdk
        minSdk = androidMinSdk
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.ghost.ktor)
            implementation(libs.icons.lucide.cmp)
            implementation(libs.jetbrains.markdown)
            implementation(libs.kermit)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.koog.agents.core)
            implementation(libs.koog.agents.tools)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.markdown.renderer.code)
            implementation(libs.markdown.renderer.m3)
            implementation(libs.multiplatform.settings)
            implementation(libs.okio)
            implementation(libs.sqldelight.runtime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.multiplatform.settings.test)
            implementation(libs.okio.fakefilesystem)
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
            implementation(libs.compose.ui.test)
        }
    }
}

dependencies {
    ktlintRuleset(project(":ktlint-rules"))
    // Preview rendering for Android Studio; on the KMP library plugin it goes on the runtime classpath.
    "androidRuntimeClasspath"(libs.compose.ui.tooling)
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
        // Generated sources (Compose resources, KSP) live under build/. Compare files, not path strings, so the
        // exclusion also works with Windows backslash separators.
        val buildDirectory = layout.buildDirectory.get().asFile
        exclude { element -> element.file.startsWith(buildDirectory) }
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

// `./gradlew :composeApp:run -Pgallery` opens the design system gallery instead of the app (development only).
tasks.withType<JavaExec>().matching { it.name == "run" }.configureEach {
    if (project.hasProperty(galleryGradleProperty)) {
        systemProperty(gallerySystemProperty, true)
    }
}

compose.resources {
    packageOfResClass = "app.notekea.resources"
    publicResClass = false
}

compose.desktop {
    application {
        mainClass = mainClassName
        // Package the toolchain's JDK, not the one that runs Gradle, so the installers ship the JDK the app is built
        // and tested on.
        javaHome = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(jdkVersion) }
            .get().metadata.installationPath.asFile.absolutePath
        // Skiko and java-llama.cpp load native libraries; without this the JVM warns on every launch.
        jvmArgs += nativeAccessJvmArgument
        jvmArgs += "-D$appVersionSystemProperty=$appDisplayVersion"

        nativeDistributions {
            targetFormats(
                TargetFormat.Deb,
                TargetFormat.Dmg,
                TargetFormat.Msi
            )
            packageName = appPackageName
            packageVersion = appPackageVersion
            description = appDescription
            vendor = appVendor
            modules(*runtimeModules.toTypedArray())

            // Each installer adds Notekea to the system's launcher with the app's logo: the Start menu and the desktop
            // on Windows, Applications (and so Launchpad) on macOS, the applications menu on Linux.
            windows {
                iconFile.set(project.file("packaging/icon.ico"))
                menu = true
                menuGroup = appPackageName
                shortcut = true
                upgradeUuid = windowsUpgradeUuid
            }
            macOS {
                iconFile.set(project.file("packaging/icon.icns"))
                bundleID = appBundleId
                dockName = appPackageName
            }
            linux {
                iconFile.set(project.file("packaging/icon.png"))
                shortcut = true
                // jpackage writes the menu group as the desktop entry's category, the menu folder Notekea shows in.
                menuGroup = linuxAppCategory
                appCategory = linuxAppCategory
            }
        }
    }
}

val checkRuntimeModules = tasks.register<CheckRuntimeModulesTask>("checkRuntimeModules") {
    description = "Fails when the packaged runtime lacks a JDK module the app needs"
    group = "verification"
    val suggest = tasks.named<org.jetbrains.compose.desktop.application.tasks.AbstractSuggestModulesTask>(
        "suggestRuntimeModules"
    )
    classpath.from(suggest.map { it.files })
    mainJar.set(suggest.flatMap { it.launcherMainJar })
    javaHome.set(suggest.flatMap { it.javaHome })
    jvmTarget.set(suggest.flatMap { it.jvmTarget })
    // The packaged runtime's modules: runtimeModules plus the ones Compose always adds.
    packagedModules.set(suggest.flatMap { it.modules })
}

tasks.named("check") { dependsOn(checkRuntimeModules) }
