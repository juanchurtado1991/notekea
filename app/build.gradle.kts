plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    alias(libs.plugins.ksp)
    alias(libs.plugins.ghost)
    alias(libs.plugins.sqldelight)
    id("notekea.jvm")
    alias(libs.plugins.kover)
}

private val modelTestPattern = "*RealModelTest"
private val chatModelTestPattern = "*ChatModelTest"
private val chatModelsGradleProperty = "chatModels"
private val chatModelsSystemProperty = "notekea.chat.models"
private val appPackageName = "Notekea"
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

kotlin {
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
            implementation(libs.kotlinx.coroutines.core)
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
            // Crash reports and their notice (test-buddy-crash/README.md): the one Test Buddy library the release ships.
            implementation(projects.testBuddyCrash)

            implementation(libs.directory.watcher)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.llama)
            LlamaNatives.dependencies(library = libs.llama.get()).forEach { implementation(it) }
            implementation(libs.lucene.core)
            implementation(libs.lucene.analysis.common)
            implementation(libs.sqldelight.sqlite.driver)
        }

        jvmTest.dependencies {
            implementation(libs.konsist)
            implementation(libs.compose.ui.test)
        }
    }
}

dependencies {
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

// `./gradlew :app:run -Pgallery` opens the design system gallery instead of the app (development only).
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
        notekeaDesktopApp(project = project)

        nativeDistributions {
            packageName = appPackageName
            description = appDescription
            vendor = appVendor

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

// The bundled embedding model: downloaded once (checked by its SHA-256).
val downloadEmbeddingModel = EmbeddingModel.registerDownload(project)

// The tests that load the real model (named *RealModelTest) stay out of check, which every pull request runs: they run in
// `./gradlew :app:modelTest`, in the nightly build (ARCHITECTURE.md, testing).
val jvmTest = tasks.named<Test>("jvmTest") {
    filter {
        excludeTestsMatching(modelTestPattern)
        excludeTestsMatching(chatModelTestPattern)
    }
}
tasks.register<Test>("modelTest") {
    description = "Runs the tests that load the bundled embedding model"
    group = "verification"
    testClassesDirs = jvmTest.get().testClassesDirs
    classpath = jvmTest.get().classpath
    useJUnit()
    filter { includeTestsMatching(modelTestPattern) }
    dependsOn(downloadEmbeddingModel)
    systemProperty(EmbeddingModel.TEST_PROPERTY, EmbeddingModel.file(project).asFile.absolutePath)
}

// The tests that load a chat model and its reranker (named *ChatModelTest) need files of gigabytes the build does not
// download: they run by hand, `./gradlew :app:chatModelTest -PchatModels=<folder>`, the folder holding the catalog's
// files (docs/HANDOFF.md, M3). Never in check nor in the nightly build.
tasks.register<Test>("chatModelTest") {
    description = "Runs the tests that load a chat model, from the folder given as -PchatModels"
    group = "verification"
    testClassesDirs = jvmTest.get().testClassesDirs
    classpath = jvmTest.get().classpath
    useJUnit()
    filter { includeTestsMatching(chatModelTestPattern) }
    systemProperty(chatModelsSystemProperty, providers.gradleProperty(chatModelsGradleProperty).getOrElse(""))
    systemProperty("notekea.chat.size", providers.gradleProperty("chatSize").getOrElse("small"))
    systemProperty("notekea.chat.gpu", providers.gradleProperty("chatGpu").getOrElse("true"))
    // The answer evaluation (3.4) indexes the sample vaults with the bundled embedding model.
    dependsOn(downloadEmbeddingModel)
    systemProperty(EmbeddingModel.TEST_PROPERTY, EmbeddingModel.file(project).asFile.absolutePath)
    maxHeapSize = "2g"
    outputs.upToDateWhen { false }
}
