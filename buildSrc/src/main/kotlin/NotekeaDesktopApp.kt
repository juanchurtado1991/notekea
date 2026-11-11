import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.compose.desktop.application.dsl.JvmApplication
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractSuggestModulesTask

/**
 * What Notekea and Notekea QA share as desktop apps: the toolchain's JDK in the installers, the same runtime, the same
 * versions, the embedding model bundled in and the check that the runtime holds what the app needs.
 */
fun JvmApplication.notekeaDesktopApp(project: Project) {
    // Package the toolchain's JDK, not the one that runs Gradle, so the installers ship the JDK the app is built and
    // tested on.
    javaHome = project.extensions.getByType<JavaToolchainService>()
        .launcherFor {
            languageVersion.set(JavaLanguageVersion.of(NotekeaVersions.jdk(project = project)))
        }
        .get().metadata.installationPath.asFile.absolutePath
    // Skiko and java-llama.cpp load native libraries; without this the JVM warns on every launch.
    jvmArgs += "--enable-native-access=ALL-UNNAMED"
    jvmArgs += memoryJvmArgs()
    jvmArgs += "-Dnotekea.version=${NotekeaVersions.displayVersion(project = project)}"
    nativeDistributions {
        // The embedding model goes with the app, read from compose.application.resources.dir.
        appResourcesRootDir.set(project.layout.buildDirectory.dir("app-resources"))
        targetFormats(TargetFormat.Deb, TargetFormat.Dmg, TargetFormat.Msi)
        packageVersion = NotekeaVersions.packageVersion(project = project)
        // The JDK modules the installers keep (gradle.properties).
        modules(
            *project.providers.gradleProperty(
                "notekea.runtimeModules"
            ).get().split(",").toTypedArray()
        )
    }
    val modelResources = EmbeddingModel.appResources(project)
    project.tasks.matching {
        it.name == "prepareAppResources"
    }.configureEach { dependsOn(modelResources) }
    registerRuntimeCheck(project = project)
}

/**
 * The options 2.9 and 5.4 measured to cost no speed (`docs/performance.md`): two compiler threads, a collection when
 * idle, a heap that gives back what it does not use, and on Linux a trim of the native heap. Built on each system.
 */
private fun memoryJvmArgs(): List<String> {
    val everywhere = listOf(
        "-XX:CICompilerCount=2",
        "-XX:G1PeriodicGCInterval=15000",
        "-XX:MaxHeapFreeRatio=20",
        "-XX:MinHeapFreeRatio=5"
    )
    val linux = listOf("-XX:+UnlockExperimentalVMOptions", "-XX:TrimNativeHeapInterval=10000")
    return if (System.getProperty("os.name").startsWith("Linux")) everywhere + linux else everywhere
}

/** Registers `checkRuntimeModules` in `check`, reading what Compose's `suggestRuntimeModules` knows of the app. */
private fun registerRuntimeCheck(project: Project) {
    val checkRuntimeModules = project.tasks.register<CheckRuntimeModulesTask>(
        "checkRuntimeModules"
    ) {
        description = "Fails when the packaged runtime lacks a JDK module the app needs"
        group = "verification"
        val suggest = project.tasks.named<AbstractSuggestModulesTask>("suggestRuntimeModules")
        classpath.from(suggest.map { it.files })
        mainJar.set(suggest.flatMap { it.launcherMainJar })
        javaHome.set(suggest.flatMap { it.javaHome })
        jvmTarget.set(suggest.flatMap { it.jvmTarget })
        // The packaged runtime's modules: notekea.runtimeModules plus the ones Compose always adds.
        packagedModules.set(suggest.flatMap { it.modules })
    }
    project.tasks.named("check") { dependsOn(checkRuntimeModules) }
}
