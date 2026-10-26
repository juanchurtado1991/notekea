package app.notekea.core.crash.presentation

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import app.notekea.core.crash.domain.RecordCrashUseCase
import app.notekea.core.crash.domain.RecordedCrash
import co.touchlab.kermit.Logger
import kotlinx.coroutines.runBlocking
import java.awt.event.WindowEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities

/**
 * What happens when Notekea fails (`qa/README.md`). An error in the window (composition, layout, drawing) is kept as a
 * crash report ([RecordCrashUseCase]), then, after the frame that failed, [crashNotice] says so and the window closes
 * the usual way, so the close guard saves the open note first. Compose's own handler only showed the raw message and
 * kept nothing. A window crash is handled once: a broken screen fails again on every frame. An error on another thread
 * is kept as a report too, without a notice, since the app goes on. [later] runs work after the current frame
 * (`SwingUtilities.invokeLater` in the app).
 */
class CrashReporter(
    private val recordCrash: RecordCrashUseCase,
    private val crashNotice: CrashNotice,
    private val later: (() -> Unit) -> Unit = { SwingUtilities.invokeLater(it) }
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val hasWindowCrashed = AtomicBoolean(false)

    fun installThreadHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, error -> record(error = error, threadName = thread.name) }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    fun windowExceptionHandlers(): WindowExceptionHandlerFactory = WindowExceptionHandlerFactory { window ->
        WindowExceptionHandler { error ->
            onWindowCrash(error = error, threadName = Thread.currentThread().name) {
                window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
            }
        }
    }

    /** Keeps [error] as a report, then shows the notice and runs [closeWindow]; later window errors are ignored. */
    fun onWindowCrash(
        error: Throwable,
        threadName: String,
        closeWindow: () -> Unit
    ) {
        if (!hasWindowCrashed.compareAndSet(false, true)) return
        val crash = record(error = error, threadName = threadName)
        later {
            crashNotice.show(crash = crash)
            closeWindow()
        }
    }

    private fun record(
        error: Throwable,
        threadName: String
    ): RecordedCrash {
        logger.e(throwable = error) { CRASH_MESSAGE }
        return try {
            runBlocking { recordCrash(error = error, threadName = threadName) }
        } catch (failure: Exception) {
            // Keeping the report failed too: the person can still copy what is known.
            logger.e(throwable = failure) { REPORT_FAILED_MESSAGE }
            RecordedCrash(text = error.stackTraceToString(), path = null)
        }
    }

    companion object {
        private const val CRASH_MESSAGE = "Unexpected error, kept as a crash report"
        private const val LOG_TAG = "CrashReporter"
        private const val REPORT_FAILED_MESSAGE = "The crash report could not be built"
    }
}
