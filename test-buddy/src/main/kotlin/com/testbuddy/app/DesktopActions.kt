package com.testbuddy.app

import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.nio.file.Path

/** What the Self-Test asks of the desktop: the clipboard, the file manager, the installed app and a picture to add. */
internal object DesktopActions {
    fun copy(text: String) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }

    fun open(folder: Path) {
        runCatching { Desktop.getDesktop().open(folder.toFile()) }
    }

    /**
     * Starts the installed app's program at [path] with [environment] added, its output and errors written to
     * [output], so a tester can send what it printed without opening a terminal.
     */
    fun launch(
        path: Path,
        environment: Map<String, String>,
        output: Path
    ) {
        runCatching {
            ProcessBuilder(path.toString())
                .redirectErrorStream(true)
                .redirectOutput(output.toFile())
                .apply { environment().putAll(environment) }
                .start()
        }
    }

    fun pickPicture(): Path? {
        val dialog = FileDialog(null as Frame?, "Add a screenshot", FileDialog.LOAD)
        dialog.isVisible = true
        val name = dialog.file ?: return null
        return Path.of(dialog.directory, name)
    }
}
