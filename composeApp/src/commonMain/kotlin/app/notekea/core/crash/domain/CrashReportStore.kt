package app.notekea.core.crash.domain

import app.notekea.core.domain.Outcome

/** Where crash reports are kept: one text file per crash, only the most recent ones. */
interface CrashReportStore {
    /** Writes [text] as the report [name] and returns where it was written. */
    suspend fun save(
        name: String,
        text: String
    ): Outcome<String, CrashReportError>
}
