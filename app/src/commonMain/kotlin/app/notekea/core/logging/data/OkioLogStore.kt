package app.notekea.core.logging.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.logging.domain.LogStore
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * [LogStore] on Okio: the `logs` folder of the app data directory, where the current file is [CURRENT_FILE] and older
 * ones `notekea.1.log`, `notekea.2.log`… (the higher, the older), as [LogFiles] names them.
 */
class OkioLogStore(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers,
    private val appDataDirectory: String
) : LogStore {
    override suspend fun recentLines(
        count: Int
    ): List<String> = withContext(context = dispatchers.io) {
        val folder = appDataDirectory.toPath() / LogFiles.FOLDER
        val oldestFirst = (LogFiles.KEPT - 1 downTo 0).map { folder / LogFiles.name(index = it) }
        oldestFirst.flatMap { linesOf(file = it) }.takeLast(n = count)
    }

    private fun linesOf(
        file: Path
    ): List<String> = try {
        if (fileSystem.exists(path = file)) {
            fileSystem.read(file = file) { readUtf8() }.lines().filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    } catch (_: IOException) {
        emptyList()
    }

    companion object {
        val CURRENT_FILE = LogFiles.name(index = 0)
    }
}
