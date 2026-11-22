package com.testbuddy.crash

import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Clock
import java.time.temporal.ChronoUnit

/**
 * Writes a crash's report: the app and its version, when, the [AppEnvironment], the thread, the person's last steps
 * ([Breadcrumbs]) and the error with its causes. An error's message could carry the person's text, so every line is cut
 * at [MAX_LINE_LENGTH] characters and the report at [MAX_LINES] lines. Only the [KEPT] newest reports stay.
 */
public object CrashReports {
    public const val KEPT: Int = 10
    public const val MAX_LINE_LENGTH: Int = 300
    public const val MAX_LINES: Int = 400

    public fun record(
        config: CrashConfig,
        error: Throwable,
        threadName: String,
        clock: Clock = Clock.systemUTC(),
        environment: AppEnvironment = AppEnvironment.current(),
        steps: List<String> = Breadcrumbs.recent()
    ): CrashReport {
        val time = clock
            .instant()
            .truncatedTo(ChronoUnit.SECONDS)
            .toString()

        val text = text(config, error, threadName, time, environment, steps)

        val path = try {
            save(
                folder = config.reportFolder,
                stem = "crash-${time.replace(':', '-')}",
                text = text
            )
        } catch (_: IOException) {
            null
        }

        return CrashReport(
            text = text,
            path = path
        )
    }

    private fun text(
        config: CrashConfig,
        error: Throwable,
        threadName: String,
        time: String,
        environment: AppEnvironment,
        steps: List<String>
    ): String {
        val header =
            listOf("${config.appName} crash report", "Version: ${config.version}", "Time: $time") +
                environment.lines() + "Thread: $threadName"

        val lastSteps = listOf("", "Last steps:") + steps.ifEmpty { listOf("(none recorded)") }

        val trace = listOf("") + error
            .stackTraceToString()
            .lines()
            .filter { it.isNotBlank() }

        return (header + lastSteps + trace)
            .take(n = MAX_LINES)
            .joinToString(
                separator = "\n",
                postfix = "\n"
            ) {
                if (it.length > MAX_LINE_LENGTH) {
                    it.take(MAX_LINE_LENGTH) + "…"
                } else {
                    it
                }
            }
    }

    // The code review of 2026-11-22: two errors in one second share a name, and the second replaced the first, the
    // root cause. A report only goes to a file that is not there yet: "crash-….txt", then "crash-…-2.txt" and on.
    private fun save(
        folder: Path,
        stem: String,
        text: String
    ): String {
        Files.createDirectories(folder)

        val file = generateSequence(seed = 1) {
            it + 1
        }
            .map { folder.resolve(if (it == 1) "$stem.txt" else "$stem-$it.txt") }
            .first {
                isCreated(
                    file = it,
                    text = text
                )
            }

        Files.list(folder).use { files ->
            files
                .filter {
                    it
                        .fileName
                        .toString()
                        .startsWith("crash-")
                }
                .sorted(Comparator.reverseOrder())
                .skip(KEPT.toLong())
                .forEach { Files.deleteIfExists(it) }
        }

        return file.toString()
    }

    private fun isCreated(
        file: Path,
        text: String
    ): Boolean = try {
        Files.writeString(file, text, StandardOpenOption.CREATE_NEW)
        true
    } catch (_: FileAlreadyExistsException) {
        false
    }
}
