package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.notes.domain.FileOpener
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.withContext
import kotlin.io.path.Path
import kotlin.io.path.exists

/**
 * [FileOpener] with each system's own command, as a double click in the file manager: `open` on macOS, `start` on
 * Windows and `xdg-open` on Linux. A file that no longer exists is reported instead.
 * [runCommand] runs a command and returns its exit code, or null when it could not run (see `ProcessCommands.kt`).
 */
class ProcessFileOpener(
    private val operatingSystem: OperatingSystem,
    private val runCommand: (List<String>) -> Int?,
    private val dispatchers: AppDispatchers
) : FileOpener {
    override suspend fun open(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> = withContext(context = dispatchers.io) {
        val file = Path(vaultPath, path.value)
        if (!file.exists()) return@withContext Outcome.Failure(error = NotesError.EntryMissing(path = path.value))
        val command = when (operatingSystem) {
            OperatingSystem.LINUX -> listOf(XDG_OPEN, file.toString())
            OperatingSystem.MACOS -> listOf(OPEN, file.toString())
            OperatingSystem.WINDOWS -> listOf(CMD, CMD_RUN, START, START_TITLE, file.toString())
        }
        if (runCommand(command) == SUCCESS) {
            Outcome.Success(value = Unit)
        } else {
            Outcome.Failure(error = NotesError.OpenFailed(path = path.value))
        }
    }

    companion object {
        private const val CMD = "cmd"
        private const val CMD_RUN = "/c"
        private const val OPEN = "open"
        private const val START = "start"

        // `start` takes its first quoted argument as the window title, so an empty one comes before the file.
        private const val START_TITLE = "\"\""
        private const val SUCCESS = 0
        private const val XDG_OPEN = "xdg-open"
    }
}
