package app.notekea.core.crash.data

import app.notekea.core.crash.domain.CrashReportError
import app.notekea.core.crash.domain.CrashReportStore
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath

/**
 * [CrashReportStore] on Okio, in the `crashes` folder of the app data directory. After each report only the
 * [MAX_REPORTS] most recent are kept; report names start with their time, so the newest sort last.
 */
class OkioCrashReportStore(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers,
    private val appDataDirectory: String
) : CrashReportStore {
    override suspend fun save(
        name: String,
        text: String
    ): Outcome<String, CrashReportError> = withContext(context = dispatchers.io) {
        val folder = appDataDirectory.toPath() / FOLDER_NAME
        try {
            fileSystem.createDirectories(dir = folder)
            val file = folder / name
            fileSystem.write(file = file) { writeUtf8(string = text) }
            removeOldReports(folder = folder)
            Outcome.Success(value = file.toString())
        } catch (_: IOException) {
            Outcome.Failure(error = CrashReportError.WriteFailed)
        }
    }

    private fun removeOldReports(
        folder: okio.Path
    ) {
        fileSystem.list(dir = folder)
            .filter { it.name.startsWith(prefix = REPORT_PREFIX) && it.name.endsWith(suffix = REPORT_EXTENSION) }
            .sortedByDescending { it.name }
            .drop(n = MAX_REPORTS)
            .forEach { fileSystem.delete(path = it, mustExist = false) }
    }

    companion object {
        /** Ten reports cover a bad day without letting a crash loop fill the disk. */
        const val MAX_REPORTS = 10
        const val FOLDER_NAME = "crashes"
        private const val REPORT_EXTENSION = ".txt"
        private const val REPORT_PREFIX = "crash-"
    }
}
