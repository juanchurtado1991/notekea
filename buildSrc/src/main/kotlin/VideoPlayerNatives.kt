import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/**
 * ComposeMediaPlayer's native library goes with the app, in its resources' `natives` folder, and the JVM starts with
 * it on `java.library.path`. The library otherwise copies it to `~/.cache/composemediaplayer` on the first video, and
 * where that folder cannot be written (a `~/.cache` made by root, as on the developer's Mac) no video plays.
 */
object VideoPlayerNatives {
    private const val FOLDER = "natives"
    private const val JAR = "mediaplayer-jvm"
    private const val RESOURCE_ROOT = "composemediaplayer/native"
    private const val APP_RESOURCES = "\$APPDIR/resources"

    /** The JVM option of the installed app: `$APPDIR` is where jpackage puts the app's files. */
    const val LIBRARY_PATH = "-Djava.library.path=$APP_RESOURCES/$FOLDER"

    /**
     * Registers in [project] the task that takes this system's library out of the player's jar into the app's
     * resources, from the [classpath] configuration, and makes `run` read it from the resources it prepares.
     */
    fun appResources(
        project: Project,
        classpath: String
    ): TaskProvider<Sync> {
        val natives = project.tasks.register<Sync>("videoPlayerNatives") {
            description = "Puts ComposeMediaPlayer's native library in the app's resources"
            val jars = project.configurations.getByName(classpath)
            from(
                project.provider {
                    jars.filter { it.name.startsWith(JAR) }.map { project.zipTree(it) }
                }
            ) {
                include("$RESOURCE_ROOT/${platform()}/*")
                eachFile { path = name }
                includeEmptyDirs = false
            }
            into(project.layout.buildDirectory.dir("app-resources/${EmbeddingModel.COMMON_RESOURCES}/$FOLDER"))
        }

        project.tasks.withType<JavaExec>().matching { it.name == "run" }.configureEach {
            doFirst {
                val resources = jvmArgs.orEmpty()
                    .firstOrNull { it.startsWith("-Dcompose.application.resources.dir=") }
                    ?.substringAfter("=")
                    ?: return@doFirst

                jvmArgs = jvmArgs.orEmpty().map { it.replace(APP_RESOURCES, resources) }
            }
        }

        return natives
    }

    // The folder names ComposeMediaPlayer's jar uses for each system.
    private fun platform(): String {
        val os = System.getProperty("os.name").lowercase()
        val isArm = System.getProperty("os.arch").let { "aarch64" in it || "arm" in it }

        return when {
            "win" in os -> if (isArm) "win32-arm64" else "win32-x86-64"
            "mac" in os -> if (isArm) "darwin-aarch64" else "darwin-x86-64"
            else -> if (isArm) "linux-aarch64" else "linux-x86-64"
        }
    }
}
