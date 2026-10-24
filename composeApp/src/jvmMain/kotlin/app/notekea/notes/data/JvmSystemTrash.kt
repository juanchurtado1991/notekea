package app.notekea.notes.data

import app.notekea.core.platform.domain.OperatingSystem
import java.awt.Desktop
import java.io.File

/**
 * The operating system's trash on desktop. macOS and Windows go through `java.awt.Desktop`; the JDK has no trash on
 * Linux, so there it asks `gio trash` (GLib, present on GNOME, COSMIC and most desktops) to follow the freedesktop.org
 * trash specification. Every failure answers false, so the caller can fall back to the vault's own trash folder.
 * [runCommand] runs a command and returns its exit code (see `ProcessCommands.kt`).
 */
class JvmSystemTrash(
    private val operatingSystem: OperatingSystem,
    private val runCommand: (List<String>) -> Int?
) {
    fun moveToTrash(
        absolutePath: String
    ): Boolean = when (operatingSystem) {
        // "--" ends the options, so a file name starting with "-" is never read as one.
        OperatingSystem.LINUX -> runCommand(listOf(GIO, GIO_TRASH, END_OF_OPTIONS, absolutePath)) == SUCCESS

        OperatingSystem.MACOS, OperatingSystem.WINDOWS -> moveWithDesktop(file = File(absolutePath))
    }

    private fun moveWithDesktop(
        file: File
    ): Boolean = try {
        val canUseTrash = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH)
        canUseTrash && Desktop.getDesktop().moveToTrash(file)
    } catch (_: SecurityException) {
        false
    } catch (_: UnsupportedOperationException) {
        false
    }

    companion object {
        private const val END_OF_OPTIONS = "--"
        private const val GIO = "gio"
        private const val GIO_TRASH = "trash"
        private const val SUCCESS = 0
    }
}
