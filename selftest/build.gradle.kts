import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractSuggestModulesTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

// Notekea's Self-Test (qa/SELF_TEST.md): its cases on Test Buddy, the smoke data bundled in, and the
// "Notekea Self-Test" installers, packaged with Notekea's own Java runtime.
private val jdkVersion = 21
private val appName = "Notekea Self-Test"
private val appPackageVersion = "1.0.0"
private val appDisplayVersion = "0.1.0-alpha"
private val mainClassName = "app.notekea.selftest.MainKt"
private val runtimeModules = providers.gradleProperty("notekea.runtimeModules").get().split(",")
private val smokeDataFolders = listOf("vaults", "notes", "daily", "templates", "attachments", "outside")
private val bundledSmokeData = layout.buildDirectory.dir("generated/smoke-data")

kotlin {
    jvmToolchain(jdkVersion)

    compilerOptions {
        allWarningsAsErrors.set(true)
        optIn.add("androidx.compose.ui.test.ExperimentalTestApi")
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":test-buddy"))
    implementation(compose.desktop.currentOs)
    implementation(libs.koin.core)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.okio)

    ktlintRuleset(project(":ktlint-rules"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

// The smoke data travels inside the Self-Test, with an index of its files: a jar cannot list a folder.
val bundleSmokeData = tasks.register<Sync>("bundleSmokeData") {
    from(rootProject.file("smoke-data")) { smokeDataFolders.forEach { include("$it/**") } }
    into(bundledSmokeData.map { it.dir("smoke-data") })
    doLast {
        val root = destinationDir
        val files = root.walkTopDown().filter { it.isFile }.map { it.relativeTo(root).invariantSeparatorsPath }.sorted()
        root.resolve("index.txt").writeText(files.joinToString(separator = "\n", postfix = "\n"))
    }
}
sourceSets.main { resources.srcDir(files(bundledSmokeData).builtBy(bundleSmokeData)) }

ktlint {
    version.set(libs.versions.ktlint.cli.get())
    verbose.set(true)
    ignoreFailures.set(false)
}

tasks.test {
    useJUnitPlatform()
}

// The automated smoke runs alone (ROADMAP.md 1.16), recording the process's memory for a performance pass:
// `./gradlew :selftest:smokeRun -PidleSeconds=600` writes build/reports/smoke-runs/memory.csv, with the JVM options of
// `-PsmokeJvmArgs` when given. They also run, without recording, in every `test`.
tasks.register<Test>("smokeRun") {
    description = "Runs the automated smoke runs and records the memory of each"
    group = "verification"
    val test = tasks.test.get()
    testClassesDirs = test.testClassesDirs
    classpath = test.classpath
    useJUnitPlatform()
    filter { includeTestsMatching("app.notekea.smokerun.*") }
    systemProperty("notekea.smokeRun.reportDir", layout.buildDirectory.dir("reports/smoke-runs").get().asFile.path)
    systemProperty("notekea.smokeRun.idleSeconds", providers.gradleProperty("idleSeconds").getOrElse("0"))
    // A performance pass compares JVM settings: `-PsmokeJvmArgs="-XX:+UseSerialGC -Xmx256m"`.
    providers.gradleProperty("smokeJvmArgs").orNull?.let { jvmArgs(it.split(" ").filter(String::isNotBlank)) }
    // And the C library's memory arenas on Linux: `-PmallocArenaMax=2`.
    providers.gradleProperty("mallocArenaMax").orNull?.let { environment("MALLOC_ARENA_MAX", it) }
    // A fresh JVM per test class, so a run is measured without what the runs before it left behind.
    forkEvery = 1
    outputs.upToDateWhen { false }
}

compose.desktop {
    application {
        mainClass = mainClassName
        javaHome = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(jdkVersion) }
            .get().metadata.installationPath.asFile.absolutePath
        jvmArgs += "--enable-native-access=ALL-UNNAMED"
        jvmArgs += "-Dnotekea.version=$appDisplayVersion"

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Dmg, TargetFormat.Msi)
            packageName = appName
            packageVersion = appPackageVersion
            description = "Checks Notekea on this computer before a release"
            vendor = "Notekea"
            modules(*runtimeModules.toTypedArray())
            windows {
                iconFile.set(rootProject.file("composeApp/packaging/icon.ico"))
                menu = true
                menuGroup = "Notekea"
                // Never change it: a newer Self-Test installer updates the installed one by this ID.
                upgradeUuid = "3c2f0b7e-5a41-4d0e-9a8e-6f1d2b7c4e19"
            }
            macOS {
                iconFile.set(rootProject.file("composeApp/packaging/icon.icns"))
                bundleID = "app.notekea.selftest"
            }
            linux {
                iconFile.set(rootProject.file("composeApp/packaging/icon.png"))
                shortcut = true
                menuGroup = "Development"
            }
        }
    }
}

// `./gradlew :selftest:run -Pcases=1.6,1.17-02` opens the Self-Test with only those cases (a feature's smoke test).
tasks.withType<JavaExec>().matching { it.name == "run" }.configureEach {
    providers.gradleProperty("cases").orNull?.let { systemProperty("notekea.selftest.cases", it) }
}

// `./gradlew :selftest:packageSelfTest` builds the installer of this system, like packageDistributionForCurrentOS.
tasks.register("packageSelfTest") {
    description = "Builds the Notekea Self-Test installer of this system"
    group = "distribution"
    dependsOn("packageDistributionForCurrentOS")
}

val checkRuntimeModules = tasks.register<CheckRuntimeModulesTask>("checkRuntimeModules") {
    description = "Fails when the packaged runtime lacks a JDK module the Self-Test needs"
    group = "verification"
    val suggest = tasks.named<AbstractSuggestModulesTask>("suggestRuntimeModules")
    classpath.from(suggest.map { it.files })
    mainJar.set(suggest.flatMap { it.launcherMainJar })
    javaHome.set(suggest.flatMap { it.javaHome })
    jvmTarget.set(suggest.flatMap { it.jvmTarget })
    packagedModules.set(suggest.flatMap { it.modules })
}

tasks.named("check") { dependsOn(checkRuntimeModules) }
