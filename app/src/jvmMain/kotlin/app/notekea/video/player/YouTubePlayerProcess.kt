package app.notekea.video.player

import app.notekea.core.notes.domain.YouTubeLinks
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * Starts the YouTube player window ([YouTubePlayerWindow]) for a video, as Notekea started again with the video in its
 * environment: in development `java` with this process's class path and main class, once packaged the installed
 * program itself. One window at a time: playing another closes the one before, and closing Notekea closes it.
 */
class YouTubePlayerProcess {
    private var running: Process? = null

    fun play(
        id: String,
        title: String
    ) {
        running?.destroy()

        running = ProcessBuilder(command())
            .apply {
                environment()[YouTubePlayerWindow.ADDRESS_VARIABLE] = YouTubeLinks.playerOf(id = id)
                environment()[YouTubePlayerWindow.TITLE_VARIABLE] = title
                redirectErrorStream(true)
                redirectOutput(ProcessBuilder.Redirect.DISCARD)
            }
            .start()
    }

    fun close() {
        running?.destroy()
        running = null
    }

    private fun command(): List<String> {
        val java =
            Path.of(System.getProperty("java.home"), "bin", if (isWindows()) "java.exe" else "java")

        val mainClass = System
            .getProperty("sun.java.command")
            .orEmpty()
            .substringBefore(delimiter = ' ')

        val isDevelopment =
            Files.isExecutable(java) &&
                mainClass.isNotBlank() &&
                !mainClass.endsWith(suffix = ".jar")

        if (!isDevelopment) {
            return listOf(
                ProcessHandle
                    .current()
                    .info()
                    .command()
                    .orElseThrow()
            )
        }

        val options = ManagementFactory
            .getRuntimeMXBean()
            .inputArguments
            .filterNot { it.startsWith(prefix = "-agentlib") }

        return listOf(java.toString()) + options + listOf("-cp", System.getProperty("java.class.path"), mainClass)
    }

    private fun isWindows(): Boolean = File.separatorChar == '\\'
}
