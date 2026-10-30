package com.testbuddy.crash

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Writes a crash's report: the app and its version, when, the [AppEnvironment], the thread, the person's last steps
 * ([Breadcrumbs]) and the error with its causes. An error's message could carry some of the person's text, so every
 * line is cut at [MAX_LINE_LENGTH] characters and the report at [MAX_LINES] lines. Reports go to the config's folder,
 * named by their time, and only the [KEPT] newest stay; a report that cannot be written can still be copied.
 */
public object CrashReports {
    public const val KEPT: Int = 10
    public const val MAX_LINE_LENGTH: Int = 300
    public const val MAX_LINES: Int = 400

    public fun record(
        config: CrashConfig,
        error: Throwable,
        threadName: String,
        now: Instant = Instant.now(),
        environment: AppEnvironment = AppEnvironment.current(),
        steps: List<String> = Breadcrumbs.recent()
    ): CrashReport {
        val time = now.truncatedTo(ChronoUnit.SECONDS).toString()
        val text = text(config, error, threadName, time, environment, steps)
        val path = try {
            save(folder = config.reportFolder, name = "crash-${time.replace(':', '-')}.txt", text = text)
        } catch (_: IOException) {
            null
        }
        return CrashReport(text = text, path = path)
    }

    private fun text(
        config: CrashConfig,
        error: Throwable,
        threadName: String,
        time: String,
        environment: AppEnvironment,
        steps: List<String>
    ): String {
        val header = listOf("${config.appName} crash report", "Version: ${config.version}", "Time: $time") +
            environment.lines() + "Thread: $threadName"
        val lastSteps = listOf("", "Last steps:") + steps.ifEmpty { listOf("(none recorded)") }
        val trace = listOf("") + error.stackTraceToString().lines().filter { it.isNotBlank() }
        return (header + lastSteps + trace).take(n = MAX_LINES)
            .joinToString(separator = "\n", postfix = "\n") {
                if (it.length >
                    MAX_LINE_LENGTH
                ) {
                    it.take(MAX_LINE_LENGTH) + "…"
                } else {
                    it
                }
            }
    }

    private fun save(
        folder: Path,
        name: String,
        text: String
    ): String {
        Files.createDirectories(folder)
        val file = folder.resolve(name)
        Files.writeString(file, text)
        Files.list(folder).use { files ->
            files.filter { it.fileName.toString().startsWith("crash-") }.sorted(Comparator.reverseOrder())
                .skip(KEPT.toLong()).forEach { Files.deleteIfExists(it) }
        }
        return file.toString()
    }
}
