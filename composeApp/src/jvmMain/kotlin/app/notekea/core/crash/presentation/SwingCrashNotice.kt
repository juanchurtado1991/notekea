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
import javax.swing.JOptionPane

/**
 * The crash notice as a plain Swing dialog, only when the Compose notice itself fails: it looks foreign to the app,
 * but it does not depend on Compose. Same texts and actions as [CrashNoticeContent]; it returns once Close is chosen.
 */
class SwingCrashNotice(private val actions: CrashReportActions) : CrashNotice {
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
                COPY_OPTION -> actions.copy(text = crash.text)
                REPORT_OPTION -> actions.report(text = crash.text)
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
        private const val CLOSE_OPTION = 2
        private const val COPY_OPTION = 0
        private const val REPORT_OPTION = 1
    }
}
