package app.notekea.notes.data

import app.notekea.core.platform.domain.OperatingSystem
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * The operating system's trash on desktop. macOS and Windows go through `java.awt.Desktop`; the JDK has no trash on
 * Linux, so there it asks `gio trash` (GLib, present on GNOME, COSMIC and most desktops) to follow the freedesktop.org
 * trash specification. Every failure answers false, so the caller can fall back to the vault's own trash folder.
 */
class JvmSystemTrash(private val operatingSystem: OperatingSystem) {
    fun moveToTrash(
        absolutePath: String
    ): Boolean = when (operatingSystem) {
        OperatingSystem.LINUX -> moveWithGio(absolutePath = absolutePath)
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

    private fun moveWithGio(
        absolutePath: String
    ): Boolean = try {
        // "--" ends the options, so a file name starting with "-" is never read as one.
        val process = ProcessBuilder(GIO, GIO_TRASH, END_OF_OPTIONS, absolutePath)
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start()
        val hasFinished = process.waitFor(GIO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        if (!hasFinished) process.destroy()
        hasFinished && process.exitValue() == 0
    } catch (_: IOException) {
        false
    }

    companion object {
        private const val END_OF_OPTIONS = "--"
        private const val GIO = "gio"
        private const val GIO_TIMEOUT_SECONDS = 10L
        private const val GIO_TRASH = "trash"
    }
}
