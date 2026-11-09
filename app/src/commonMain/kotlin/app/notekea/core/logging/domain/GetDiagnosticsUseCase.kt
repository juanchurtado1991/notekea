package app.notekea.core.logging.domain

import app.notekea.core.domain.StringPatterns.NEW_LINE
import app.notekea.core.domain.StringPatterns.SPACE
import app.notekea.core.platform.domain.SystemEnvironment

/**
 * What About's Copy diagnostics puts on the clipboard for a tester's report (`qa/README.md`, piece 6): the app, the
 * operating system and Java versions, and the last [LOG_LINES] lines of the app's log. The log never holds note text
 * (`ARCHITECTURE.md` §9), so neither does this.
 */
class GetDiagnosticsUseCase(
    private val systemEnvironment: SystemEnvironment,
    private val logStore: LogStore
) {
    suspend operator fun invoke(): String {
        val header = listOf(
            TITLE,
            VERSION_LABEL + systemEnvironment.appVersion,
            OS_LABEL + systemEnvironment.operatingSystemName + SPACE + systemEnvironment.operatingSystemVersion,
            JAVA_LABEL + systemEnvironment.javaVersion
        )
        val log = logStore.recentLines(count = LOG_LINES).ifEmpty { listOf(NO_LOG) }
        return (header + LOG_TITLE + log).joinToString(separator = NEW_LINE, postfix = NEW_LINE)
    }

    companion object {
        /** Enough to see what led to a problem, small enough to paste in a chat. */
        const val LOG_LINES = 100
        private const val TITLE = "Notekea diagnostics"
        private const val VERSION_LABEL = "Version: "
        private const val OS_LABEL = "OS: "
        private const val JAVA_LABEL = "Java: "
        private const val LOG_TITLE = "Recent log:"
        private const val NO_LOG = "(nothing logged yet)"
    }
}
