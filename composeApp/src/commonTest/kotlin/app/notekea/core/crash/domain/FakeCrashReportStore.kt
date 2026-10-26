package app.notekea.core.crash.domain

import app.notekea.core.domain.Outcome

/** Crash reports in memory, under a fake folder; with [isBroken] every write fails as a read-only disk would. */
class FakeCrashReportStore : CrashReportStore {
    val reports = mutableMapOf<String, String>()
    var isBroken = false

    override suspend fun save(
        name: String,
        text: String
    ): Outcome<String, CrashReportError> {
        if (isBroken) return Outcome.Failure(error = CrashReportError.WriteFailed)
        reports[name] = text
        return Outcome.Success(value = FOLDER + name)
    }

    companion object {
        const val FOLDER = "/home/ana/.local/share/notekea/crashes/"
    }
}
