package com.testbuddy.crash

import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.IOException
import java.net.URI

/**
 * What the notice's buttons do, the same in the Compose dialog and its Swing fallback: copy the report, and open the
 * app's issue form in the browser, where the copied report is pasted. Nothing is sent from here: the report only
 * leaves the computer if the person pastes and submits it.
 */
internal object CrashActions {
    fun copy(text: String) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }

    /** Copies [text] and opens [url], through the desktop API or `xdg-open` where it is missing (some Linux). */
    fun report(
        text: String,
        url: String
    ) {
        copy(text = text)
        val canBrowse = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
        try {
            if (canBrowse) Desktop.getDesktop().browse(URI(url)) else ProcessBuilder("xdg-open", url).start()
        } catch (_: IOException) {
            // No browser could be opened; the report is on the clipboard and Copy details is still there.
        }
    }
}
