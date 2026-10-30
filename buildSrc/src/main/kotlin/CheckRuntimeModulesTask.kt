import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * Asks jdeps which JDK modules an app and its libraries need, as Compose's `suggestRuntimeModules` does, and fails when
 * one is missing from [packagedModules]: without it the installed app crashes the first time it reaches that code
 * (issue #68). Shared by Notekea and Notekea QA, which ship the same runtime.
 */
abstract class CheckRuntimeModulesTask : DefaultTask() {
    @get:InputFiles
    abstract val classpath: ConfigurableFileCollection

    @get:InputFile
    abstract val mainJar: RegularFileProperty

    @get:Input
    abstract val javaHome: Property<String>

    @get:Input
    abstract val jvmTarget: Property<String>

    @get:Input
    abstract val packagedModules: ListProperty<String>

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun check() {
        val output = ByteArrayOutputStream()
        execOperations.exec {
            executable = File(javaHome.get(), "bin/jdeps").path
            args(
                "--print-module-deps",
                "--ignore-missing-deps",
                "--multi-release",
                jvmTarget.get(),
                "--class-path",
                classpath.files.joinToString(File.pathSeparator),
                mainJar.get().asFile.path
            )
            standardOutput = output
        }
        val needed = output.toString().trim().split(",").map(String::trim).filter(String::isNotEmpty)
        val missing = needed - packagedModules.get().toSet()
        if (missing.isNotEmpty()) {
            throw GradleException(
                "The packaged runtime lacks JDK modules the app needs: ${missing.joinToString()}. " +
                    "Add them to notekea.runtimeModules in gradle.properties."
            )
        }
    }
}
