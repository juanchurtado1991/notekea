package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.notes.domain.FileRevealer
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * [FileRevealer] with each system's own command: `open -R` on macOS and `explorer /select,` on Windows select the
 * entry in Finder and File Explorer. On Linux the freedesktop.org `FileManager1.ShowItems` D-Bus call selects it in
 * the user's file manager when it supports the call (Nautilus, Dolphin, Nemo…); otherwise `xdg-open` opens the
 * containing folder. File Explorer exits with 1 even when it worked, so on Windows starting it counts as success.
 * [runCommand] runs a command and returns its exit code, or null when it could not run (see `ProcessCommands.kt`).
 */
class ProcessFileRevealer(
    private val operatingSystem: OperatingSystem,
    private val runCommand: (List<String>) -> Int?,
    private val dispatchers: AppDispatchers
) : FileRevealer {
    override suspend fun reveal(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> = withContext(context = dispatchers.io) {
        val entry = if (path.isRoot) Path(vaultPath) else Path(vaultPath, path.value)
        val isRevealed = when (operatingSystem) {
            OperatingSystem.LINUX -> revealOnLinux(entry = entry)
            OperatingSystem.MACOS -> runCommand(listOf(OPEN, OPEN_REVEAL, entry.toString())) == SUCCESS
            OperatingSystem.WINDOWS -> runCommand(listOf(EXPLORER, EXPLORER_SELECT + entry.toString())) != null
        }
        if (isRevealed) {
            Outcome.Success(
                value = Unit
            )
        } else {
            Outcome.Failure(error = NotesError.RevealFailed(path = path.value))
        }
    }

    private fun revealOnLinux(
        entry: Path
    ): Boolean {
        val showItems = listOf(
            DBUS_SEND,
            DBUS_SESSION,
            DBUS_DESTINATION,
            DBUS_METHOD_CALL,
            DBUS_OBJECT_PATH,
            DBUS_SHOW_ITEMS,
            // dbus-send splits array items on commas, so a comma in a file name must stay escaped.
            DBUS_URIS_PREFIX + entry.toUri().toString().replace(oldValue = COMMA, newValue = ESCAPED_COMMA),
            DBUS_STARTUP_ID
        )
        val folder = (entry.parent ?: entry).toString()
        return runCommand(showItems) == SUCCESS || runCommand(listOf(XDG_OPEN, folder)) == SUCCESS
    }

    companion object {
        private const val COMMA = ","
        private const val DBUS_DESTINATION = "--dest=org.freedesktop.FileManager1"
        private const val DBUS_METHOD_CALL = "--type=method_call"
        private const val DBUS_OBJECT_PATH = "/org/freedesktop/FileManager1"
        private const val DBUS_SEND = "dbus-send"
        private const val DBUS_SESSION = "--session"
        private const val DBUS_SHOW_ITEMS = "org.freedesktop.FileManager1.ShowItems"
        private const val DBUS_STARTUP_ID = "string:"
        private const val DBUS_URIS_PREFIX = "array:string:"
        private const val ESCAPED_COMMA = "%2C"
        private const val EXPLORER = "explorer.exe"
        private const val EXPLORER_SELECT = "/select,"
        private const val OPEN = "open"
        private const val OPEN_REVEAL = "-R"
        private const val SUCCESS = 0
        private const val XDG_OPEN = "xdg-open"
    }
}
