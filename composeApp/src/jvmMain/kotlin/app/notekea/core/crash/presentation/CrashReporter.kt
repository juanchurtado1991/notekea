package app.notekea.core.crash.presentation

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import app.notekea.core.crash.domain.RecordCrashUseCase
import app.notekea.core.crash.domain.RecordedCrash
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import java.awt.event.WindowEvent
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.swing.SwingUtilities

/**
 * What happens when Notekea fails (`qa/README.md`). An error in a window (composition, layout, drawing) is kept as a
 * crash report ([RecordCrashUseCase]) and published in [crash], which the app shows in its own Compose dialog window,
 * a separate scene that still works when the main window is broken. Compose's own handler only showed the raw message
 * and kept nothing. Closing the notice ([close]) closes the failed window the usual way, so the close guard saves the
 * open note. The failed window keeps failing on every frame, which is ignored; if another window fails meanwhile (the
 * notice itself), [fallbackNotice] says the same with Swing, then the app closes. An error on any other thread is kept
 * as a report too, without a notice, since the app goes on. [later] runs work after the current frame.
 */
class CrashReporter(
    private val recordCrash: RecordCrashUseCase,
    private val fallbackNotice: CrashNotice,
    private val later: (() -> Unit) -> Unit = { SwingUtilities.invokeLater(it) }
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val mutableCrash = MutableStateFlow<RecordedCrash?>(value = null)
    private val failedWindow = AtomicReference<Any?>(null)
    private val closeFailedWindow = AtomicReference<(() -> Unit)?>(null)
    private val hasFallenBack = AtomicBoolean(false)

    /** The crash to tell the person about, once a window has failed. */
    val crash: StateFlow<RecordedCrash?> = mutableCrash.asStateFlow()

    fun installThreadHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, error -> record(error = error, threadName = thread.name) }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    fun windowExceptionHandlers(): WindowExceptionHandlerFactory = WindowExceptionHandlerFactory { window ->
        WindowExceptionHandler { error ->
            onWindowCrash(error = error, threadName = Thread.currentThread().name, window = window) {
                window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
            }
        }
    }

    /** Keeps the first error of [window] and publishes it; see the class for what later errors do. */
    fun onWindowCrash(
        error: Throwable,
        threadName: String,
        window: Any,
        closeWindow: () -> Unit
    ) {
        if (failedWindow.compareAndSet(null, window)) {
            closeFailedWindow.set(closeWindow)
            mutableCrash.value = record(error = error, threadName = threadName)
            return
        }
        if (failedWindow.get() === window || !hasFallenBack.compareAndSet(false, true)) return
        val crash = mutableCrash.value ?: record(error = error, threadName = threadName)
        later {
            fallbackNotice.show(crash = crash)
            close()
        }
    }

    /** The person closed the notice: the failed window closes the usual way, once. */
    fun close() {
        closeFailedWindow.getAndSet(null)?.invoke()
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
