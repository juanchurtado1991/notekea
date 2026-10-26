package app.notekea.core.crash.presentation

import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.IOException
import java.net.URI

/**
 * What the crash notice's buttons do, the same in the Compose dialog and its Swing fallback: copy the report to the
 * clipboard, and open GitHub's crash report form in the browser, where the copied report is pasted. Nothing is sent
 * from here: the report only leaves the computer if the person pastes and submits it.
 */
class CrashReportActions {
    fun copy(
        text: String
    ) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }

    /** Copies [text] and opens the form, through the desktop API or `xdg-open` where it is missing (some Linux). */
    fun report(
        text: String
    ) {
        copy(text = text)
        val canBrowse = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
        try {
            if (canBrowse) {
                Desktop.getDesktop().browse(
                    URI(ISSUE_FORM)
                )
            } else {
                ProcessBuilder(XDG_OPEN, ISSUE_FORM).start()
            }
        } catch (_: IOException) {
            // No browser could be opened; the report is on the clipboard and Copy details is still there.
        }
    }

    companion object {
        /** GitHub's form for a crash report (`.github/ISSUE_TEMPLATE/crash_report.yml`). */
        const val ISSUE_FORM = "https://github.com/juanchurtado1991/notekea/issues/new?template=crash_report.yml"
        private const val XDG_OPEN = "xdg-open"
    }
}
