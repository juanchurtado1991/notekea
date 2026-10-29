package app.notekea.core.logging.data

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.time.Clock

/**
 * Writes what the app logs (Info and above) to `logs/notekea.log` in the app data [directory], so a tester can send it
 * with Copy diagnostics (`qa/README.md`, piece 6). Once the file reaches [LogFiles.MAX_BYTES] it becomes
 * `notekea.1.log`, the older ones move up and only [LogFiles.KEPT] files stay. Log messages never hold note text
 * (`ARCHITECTURE.md` §9); an error's message could, so it is cut at [MAX_LINE_LENGTH] characters. A file that
 * cannot be written is skipped: logging never stops the app.
 */
class FileLogWriter(
    private val directory: Path,
    private val clock: Clock
) : LogWriter() {
    private val lock = Any()

    override fun isLoggable(
        tag: String,
        severity: Severity
    ): Boolean = severity >= Severity.Info

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        val error = throwable?.let { " | ${it::class.simpleName}: ${it.message.orEmpty()}" }.orEmpty()
        val line = "${clock.now()} ${severity.name.first()} $tag: $message$error".take(n = MAX_LINE_LENGTH) + "\n"
        synchronized(lock) {
            try {
                Files.createDirectories(directory)
                val current = directory.resolve(LogFiles.name(index = 0))
                if (Files.exists(current) && Files.size(current) + line.length > LogFiles.MAX_BYTES) rotate()
                Files.writeString(current, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
            } catch (_: IOException) {
                // The log is a help, never a reason to fail: a full disk or a locked file only loses this line.
            }
        }
    }

    private fun rotate() {
        Files.deleteIfExists(directory.resolve(LogFiles.name(index = LogFiles.KEPT - 1)))
        (LogFiles.KEPT - 2 downTo 0).forEach { index ->
            val file = directory.resolve(LogFiles.name(index = index))
            if (Files.exists(file)) {
                Files.move(
                    file,
                    directory.resolve(LogFiles.name(index = index + 1)),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }
    }

    companion object {
        /** As in crash reports: a long error message is cut, so the log never carries much text from a note. */
        const val MAX_LINE_LENGTH = 300
    }
}
