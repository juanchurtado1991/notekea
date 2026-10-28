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
private val nativeAccessJvmArgument = "--enable-native-access=ALL-UNNAMED"

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

// The automated smoke runs alone (ROADMAP.md 1.16), recording the process's memory for a performance pass:
// `./gradlew :composeApp:smokeRun -PidleSeconds=600` writes build/reports/smoke-runs/memory.csv, with the JVM options
// of `-PsmokeJvmArgs` when given. They also run, without
// recording, in every `jvmTest`.
val smokeRunPackage = "app.notekea.smokerun.*"
tasks.register<Test>("smokeRun") {
    description = "Runs the automated smoke runs and records the memory of each"
    group = "verification"
    val jvmTest = tasks.named<Test>("jvmTest").get()
    testClassesDirs = jvmTest.testClassesDirs
    classpath = jvmTest.classpath
    filter { includeTestsMatching(smokeRunPackage) }
    systemProperty("notekea.smokeRun.reportDir", layout.buildDirectory.dir("reports/smoke-runs").get().asFile.path)
    systemProperty("notekea.smokeRun.idleSeconds", providers.gradleProperty("idleSeconds").getOrElse("0"))
    // A performance pass compares JVM settings: `-PsmokeJvmArgs="-XX:+UseSerialGC -Xmx256m"`.
    providers.gradleProperty("smokeJvmArgs").orNull?.let { jvmArgs(it.split(" ").filter(String::isNotBlank)) }
    // A fresh JVM per test class, so a run is measured without what the runs before it left behind.
    forkEvery = 1
    outputs.upToDateWhen { false }
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
        }
    }
}
