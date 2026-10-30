package com.testbuddy.crash

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.event.WindowEvent
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.swing.SwingUtilities

/**
 * What happens when the app fails (`test-buddy-crash/README.md`). An error in a window (composition, layout, drawing) is
 * kept as a report ([CrashReports]) and published in [crash], which [CrashNotice] shows in a dialog window of its own,
 * a separate scene that still works when the main window is broken. Closing the notice ([close]) closes the failed
 * window the usual way, so the app saves the person's work. The failed window keeps failing on every frame, which is
 * ignored; if another window fails meanwhile (the notice itself), [fallback] says the same with Swing, then the app
 * closes. An error on any other thread is kept as a report, without a notice, since the app goes on. [onError] lets
 * the app log the error; [later] runs work after the current frame.
 */
public class CrashBuddy(
    public val config: CrashConfig,
    private val onError: (Throwable) -> Unit = {},
    private val record: (Throwable, String) -> CrashReport = { error, thread ->
        CrashReports.record(config = config, error = error, threadName = thread)
    },
    private val fallback: (CrashReport) -> Unit = { SwingCrashNotice(config = config).show(crash = it) },
    private val later: (() -> Unit) -> Unit = { SwingUtilities.invokeLater(it) }
) {
    private val mutableCrash = MutableStateFlow<CrashReport?>(value = null)
    private val failedWindow = AtomicReference<Any?>(null)
    private val closeFailedWindow = AtomicReference<(() -> Unit)?>(null)
    private val hasFallenBack = AtomicBoolean(false)

    /** The crash to tell the person about, once a window has failed. */
    public val crash: StateFlow<CrashReport?> = mutableCrash.asStateFlow()

    /** Keeps an error of any thread as a report. */
    public fun installThreadHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, error -> keep(error = error, threadName = thread.name) }
    }

    /** For `LocalWindowExceptionHandlerFactory`: each window's error goes to [onWindowCrash]. */
    @OptIn(ExperimentalComposeUiApi::class)
    public fun windowExceptionHandlers(): WindowExceptionHandlerFactory = WindowExceptionHandlerFactory { window ->
        WindowExceptionHandler { error ->
            onWindowCrash(error = error, threadName = Thread.currentThread().name, window = window) {
                window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
            }
        }
    }

    /** Keeps the first error of [window] and publishes it; see the class for what later errors do. */
    public fun onWindowCrash(
        error: Throwable,
        threadName: String,
        window: Any,
        closeWindow: () -> Unit
    ) {
        if (failedWindow.compareAndSet(null, window)) {
            closeFailedWindow.set(closeWindow)
            mutableCrash.value = keep(error = error, threadName = threadName)
            return
        }
        if (failedWindow.get() === window || !hasFallenBack.compareAndSet(false, true)) return
        val crash = mutableCrash.value ?: keep(error = error, threadName = threadName)
        later {
            fallback(crash)
            close()
        }
    }

    /** The person closed the notice: the failed window closes the usual way, once. */
    public fun close() {
        closeFailedWindow.getAndSet(null)?.invoke()
    }

    private fun keep(
        error: Throwable,
        threadName: String
    ): CrashReport {
        onError(error)
        return try {
            record(error, threadName)
        } catch (failure: Exception) {
            // Keeping the report failed too: the person can still copy what is known.
            onError(failure)
            CrashReport(text = error.stackTraceToString(), path = null)
        }
    }
}
