package com.testbuddy.app

import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.nio.file.Path

/** What the manager asks of the desktop: the clipboard, the file manager and a picture to add. */
internal object DesktopActions {
    fun copy(text: String) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }

    fun open(folder: Path) {
        runCatching { Desktop.getDesktop().open(folder.toFile()) }
    }

    fun pickPicture(): Path? {
        val dialog = FileDialog(null as Frame?, "Add a screenshot", FileDialog.LOAD)
        dialog.isVisible = true
        val name = dialog.file ?: return null
        return Path.of(dialog.directory, name)
    }
}
