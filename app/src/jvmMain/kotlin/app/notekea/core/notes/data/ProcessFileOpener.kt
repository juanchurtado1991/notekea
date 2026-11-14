package app.notekea.core.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FileOpener
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.platform.data.runCommand
import app.notekea.core.platform.domain.OperatingSystem
import kotlinx.coroutines.withContext
import kotlin.io.path.Path
import kotlin.io.path.exists

/**
 * [FileOpener] with each system's own command, as a double click in the file manager: `open` on macOS, the
 * file handler on Windows and `xdg-open` on Linux. A file that no longer exists is reported instead.
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

        if (!file.exists()) {
            return@withContext Outcome.Failure(
                error = NotesError.EntryMissing(path = path.value)
            )
        }

        val command = when (operatingSystem) {
            OperatingSystem.LINUX -> listOf(XDG_OPEN, file.toString())

            OperatingSystem.MACOS -> listOf(OPEN, file.toString())

            // Never through cmd: a file named "a & calc.exe" would run what its name says (2026-11-13).
            OperatingSystem.WINDOWS -> listOf(RUNDLL, FILE_HANDLER, file.toString())
        }

        if (runCommand(command) == SUCCESS) {
            Outcome.Success(value = Unit)
        } else {
            Outcome.Failure(error = NotesError.OpenFailed(path = path.value))
        }
    }

    companion object {
        private const val OPEN = "open"

        // What a double click runs, given the file as one argument no shell reads.
        private const val RUNDLL = "rundll32"
        private const val FILE_HANDLER = "url.dll,FileProtocolHandler"
        private const val SUCCESS = 0
        private const val XDG_OPEN = "xdg-open"
    }
}
