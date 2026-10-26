package app.notekea.core.crash.presentation

import app.notekea.core.crash.domain.RecordedCrash
import app.notekea.resources.Res
import app.notekea.resources.crash_close
import app.notekea.resources.crash_copy
import app.notekea.resources.crash_message
import app.notekea.resources.crash_report_github
import app.notekea.resources.crash_report_not_saved
import app.notekea.resources.crash_report_saved
import app.notekea.resources.crash_title
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.IOException
import java.net.URI
import javax.swing.JOptionPane

/**
 * The crash notice as a native Swing dialog: when Compose is what failed, a Compose dialog could fail too. It says
 * Notekea must close and where the report is, and offers Copy details (the report to the clipboard, the dialog stays),
 * Report on GitHub (copies the report and opens the crash report form, where it is pasted: nothing leaves the
 * computer until the person submits it) and Close Notekea.
 */
class SwingCrashNotice : CrashNotice {
    override fun show(
        crash: RecordedCrash
    ) {
        val texts = runBlocking { loadTexts() }
        val message = texts.message + "\n\n" + (crash.path?.let { texts.saved + "\n" + it } ?: texts.notSaved)
        val options = arrayOf(texts.copy, texts.report, texts.close)
        while (true) {
            val choice = JOptionPane.showOptionDialog(
                null,
                message,
                texts.title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[CLOSE_OPTION]
            )
            when (choice) {
                COPY_OPTION -> copy(text = crash.text)

                REPORT_OPTION -> {
                    copy(text = crash.text)
                    openInBrowser(address = ISSUE_FORM)
                }

                else -> return
            }
        }
    }

    private suspend fun loadTexts(): Texts = Texts(
        title = getString(resource = Res.string.crash_title),
        message = getString(resource = Res.string.crash_message),
        saved = getString(resource = Res.string.crash_report_saved),
        notSaved = getString(resource = Res.string.crash_report_not_saved),
        copy = getString(resource = Res.string.crash_copy),
        report = getString(resource = Res.string.crash_report_github),
        close = getString(resource = Res.string.crash_close)
    )

    private fun copy(
        text: String
    ) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }

    /** The system browser through the desktop API, or `xdg-open` where that is missing (some Linux desktops). */
    private fun openInBrowser(
        address: String
    ) {
        val canBrowse = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
        try {
            if (canBrowse) Desktop.getDesktop().browse(URI(address)) else ProcessBuilder(XDG_OPEN, address).start()
        } catch (_: IOException) {
            // No browser could be opened; the report is on the clipboard, and Copy details is still there.
        }
    }

    /** The dialog's texts, read once per crash. */
    private class Texts(
        val title: String,
        val message: String,
        val saved: String,
        val notSaved: String,
        val copy: String,
        val report: String,
        val close: String
    )

    companion object {
        /** GitHub's form for a crash report (`.github/ISSUE_TEMPLATE/crash_report.yml`). */
        const val ISSUE_FORM = "https://github.com/juanchurtado1991/notekea/issues/new?template=crash_report.yml"
        private const val CLOSE_OPTION = 2
        private const val COPY_OPTION = 0
        private const val REPORT_OPTION = 1
        private const val XDG_OPEN = "xdg-open"
    }
}
