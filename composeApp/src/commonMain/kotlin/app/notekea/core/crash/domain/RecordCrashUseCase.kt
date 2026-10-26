package app.notekea.core.crash.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.SystemEnvironment
import kotlin.time.Clock

/**
 * Keeps a report of a crash, so a person can send it without hunting for logs (`qa/README.md`): the app, operating
 * system and Java versions, when, on which thread, and the error with its stack trace and causes. Notes are never in
 * it (`GUIDELINES.md` §13); since an error message could carry some text, every line is cut at [MAX_LINE_LENGTH]
 * characters and the report at [MAX_LINES] lines. The report is returned even when its file cannot be written, so it
 * can still be copied.
 */
class RecordCrashUseCase(
    private val crashReportStore: CrashReportStore,
    private val systemEnvironment: SystemEnvironment,
    private val clock: Clock
) {
    suspend operator fun invoke(
        error: Throwable,
        threadName: String
    ): RecordedCrash {
        val time = clock.now().toString().substringBefore(delimiter = '.').removeSuffix(suffix = "Z") + "Z"
        val text = reportText(error = error, threadName = threadName, time = time)
        val name = FILE_PREFIX + time.replace(oldChar = ':', newChar = '-') + FILE_EXTENSION
        val saved = crashReportStore.save(name = name, text = text)
        return RecordedCrash(text = text, path = (saved as? Outcome.Success)?.value)
    }

    private fun reportText(
        error: Throwable,
        threadName: String,
        time: String
    ): String {
        val header = listOf(
            TITLE,
            "Version: ${systemEnvironment.appVersion}",
            "Time: $time",
            "OS: ${systemEnvironment.operatingSystemName} ${systemEnvironment.operatingSystemVersion}",
            "Java: ${systemEnvironment.javaVersion}",
            "Thread: $threadName",
            ""
        )
        val trace = error.stackTraceToString().lines().filter { it.isNotBlank() }
        val lines = (header + trace).take(n = MAX_LINES).map { line ->
            if (line.length > MAX_LINE_LENGTH) line.take(n = MAX_LINE_LENGTH) + ELLIPSIS else line
        }
        return lines.joinToString(separator = "\n", postfix = "\n")
    }

    companion object {
        /** Longer lines (a long error message) are cut, so a report never carries much text from a note. */
        const val MAX_LINE_LENGTH = 300

        /** Enough for a deep stack trace and its causes, small enough to paste anywhere. */
        const val MAX_LINES = 400
        private const val ELLIPSIS = "…"
        private const val FILE_EXTENSION = ".txt"
        private const val FILE_PREFIX = "crash-"
        private const val TITLE = "Notekea crash report"
    }
}
