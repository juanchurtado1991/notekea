plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("notekea.jvm")
    alias(libs.plugins.kover)
}

// Notekea QA (qa/AUTO_TEST.md): Notekea with its cases on Test Buddy and the manager next to its window, the smoke
// data bundled in, and the "Notekea QA" installers, packaged with Notekea's own Java runtime. Notekea's release has
// none of it.
private val appName = "Notekea QA"
private val mainClassName = "app.notekea.autotest.QaMainKt"

private val smokeDataFolders =
    listOf("vaults", "notes", "daily", "templates", "attachments", "outside")

private val bundledSmokeData = layout.buildDirectory.dir("generated/smoke-data")
private val maxForks = 4

kotlin {
    compilerOptions { optIn.add("androidx.compose.ui.test.ExperimentalTestApi") }
}

dependencies {
    implementation(projects.app)
    implementation(projects.testBuddy)
    implementation(compose.desktop.currentOs)
    implementation(libs.koin.core)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.okio)
    // The sync cases (4.2) make a local remote and play the other computer with it.
    implementation(libs.jgit)

    // The documents run (4.8) writes a large PDF into the vault.
    testImplementation(libs.pdfbox)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

// The smoke data travels inside the Auto-Test, with an index of its files: a jar cannot list a folder.
val bundleSmokeData = tasks
    .register<Sync>("bundleSmokeData") {
        from(rootProject.file("smoke-data")) {
            smokeDataFolders.forEach { include("$it/**") }
        }

        into(
            bundledSmokeData.map { it.dir("smoke-data") }
        )

        doLast {
            val root = destinationDir

            val files = root
                .walkTopDown()
                .filter { it.isFile }
                .map { it.relativeTo(root).invariantSeparatorsPath }
                .sorted()

            root.resolve("index.txt").writeText(
                files.joinToString(
                    separator = "\n",
                    postfix = "\n"
                )
            )
        }
    }

sourceSets.main { resources.srcDir(files(bundledSmokeData).builtBy(bundleSmokeData)) }

// The cases that ask a chat model run with the models of a folder already on this machine, never downloaded:
// `./gradlew :auto-test:test -PchatModels=$HOME/models` (or `:auto-test:run`). Without it, those Notekea QA downloaded.
private val chatModels = providers.gradleProperty("chatModels").orNull

tasks.test {
    useJUnitPlatform()
    chatModels?.let { systemProperty("notekea.autotest.chatModels", it) }

    // A JVM runs several test classes, and the first runTest fixes the bound of every case's outer runTest (Test
    // Buddy's SuiteRunner raises it too late when another test came first): the cases' own timeouts still apply.
    systemProperty("kotlinx.coroutines.test.default_timeout", "20m")

    // `-Pcases=3.3,3.9-01` runs only those cases, as on `:auto-test:run`.
    providers
        .gradleProperty("cases")
        .orNull
        ?.let { systemProperty("notekea.autotest.cases", it) }

    // The Auto-Test's parts and the smoke runs are test classes of their own: up to half the cores run them at the
    // same time, each in its own JVM, since every case starts the whole app with a global Koin.
    maxParallelForks =
        (Runtime.getRuntime().availableProcessors() / 2).coerceIn(
            minimumValue = 1,
            maximumValue = maxForks
        )

    // Screenshots of failed cases land next to the test reports, which CI uploads when a check fails.
    systemProperty(
        "testbuddy.workFolder",
        layout
            .buildDirectory
            .dir("reports/tests/test-buddy")
            .get()
            .asFile
            .path
    )
}

// The automated smoke runs alone (ROADMAP.md 1.16), recording the process's memory for a performance pass:
// `./gradlew :auto-test:smokeRun -PidleSeconds=600` writes build/reports/smoke-runs/memory.csv, with the JVM options of
// `-PsmokeJvmArgs` when given. They also run, without recording, in every `test`.
tasks
    .register<Test>("smokeRun") {
        description = "Runs the automated smoke runs and records the memory of each"
        group = "verification"
        val test = tasks.test.get()
        testClassesDirs = test.testClassesDirs
        classpath = test.classpath
        useJUnitPlatform()

        filter { includeTestsMatching("app.notekea.smokerun.*") }

        systemProperty(
            "notekea.smokeRun.reportDir",
            layout
                .buildDirectory
                .dir("reports/smoke-runs")
                .get()
                .asFile
                .path
        )

        systemProperty(
            "notekea.smokeRun.idleSeconds",
            providers.gradleProperty("idleSeconds").getOrElse("0")
        )

        // A performance pass compares JVM settings: `-PsmokeJvmArgs="-XX:+UseSerialGC -Xmx256m"`.
        providers
            .gradleProperty("smokeJvmArgs")
            .orNull
            ?.let { jvmArgs(it.split(" ").filter(String::isNotBlank)) }

        // And the C library's memory arenas on Linux: `-PmallocArenaMax=2`.
        providers
            .gradleProperty("mallocArenaMax")
            .orNull
            ?.let { environment("MALLOC_ARENA_MAX", it) }

        // And the bundled embedding model instead of the stand-in, as the installed app has it: `-PrealModel`.
        providers
            .gradleProperty("realModel")
            .orNull
            ?.let {
                val resources = rootProject.file("app/build/app-resources/common")
                systemProperty("compose.application.resources.dir", resources.path)
            }

        // A fresh JVM per test class, so a run is measured without what the runs before it left behind.
        forkEvery = 1
        outputs.upToDateWhen { false }

        // Only when asked for: `check` runs the same smoke runs in `test` already, and running them twice doubled CI.
        onlyIf {
            gradle
                .startParameter
                .taskNames
                .any { it.endsWith("smokeRun") }
        }
    }

compose.desktop {
    application {
        mainClass = mainClassName
        notekeaDesktopApp(project = project)

        nativeDistributions {
            packageName = appName
            description = "Notekea with its tests, for testers before a release"
            vendor = "Notekea"

            windows {
                iconFile.set(rootProject.file("app/packaging/icon.ico"))
                menu = true
                menuGroup = "Notekea"
                // Never change it: a newer Notekea QA installer updates the installed one by this ID.
                upgradeUuid = "3c2f0b7e-5a41-4d0e-9a8e-6f1d2b7c4e19"
            }

            macOS {
                iconFile.set(rootProject.file("app/packaging/icon.icns"))
                bundleID = "app.notekea.autotest"
            }

            linux {
                iconFile.set(rootProject.file("app/packaging/icon.png"))
                shortcut = true
                menuGroup = "Development"
            }
        }
    }
}

// `./gradlew :auto-test:run -Pcases=1.6,1.17-02` opens Notekea QA with only those cases (a feature's smoke test).
tasks
    .withType<JavaExec>()
    .matching {
        it.name == "run"
    }
    .configureEach {
        providers
            .gradleProperty("cases")
            .orNull
            ?.let { systemProperty("notekea.autotest.cases", it) }

        chatModels?.let { systemProperty("notekea.autotest.chatModels", it) }
    }

// `./gradlew :auto-test:packageQa` builds the Notekea QA installer of this system, like
// packageDistributionForCurrentOS.
tasks
    .register("packageQa") {
        description = "Builds the Notekea QA installer of this system"
        group = "distribution"
        dependsOn("packageDistributionForCurrentOS")
    }
