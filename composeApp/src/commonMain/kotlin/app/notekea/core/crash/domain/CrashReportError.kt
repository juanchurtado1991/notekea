package app.notekea.core.crash.domain

/** Why a crash report could not be kept. */
sealed interface CrashReportError {
    /** The crash reports folder or the file could not be written. */
    data object WriteFailed : CrashReportError
}
