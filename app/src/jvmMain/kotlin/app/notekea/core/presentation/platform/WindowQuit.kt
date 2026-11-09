package app.notekea.core.presentation.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * Every way Notekea can end, leading through one [WindowCloseGuard] run on the window's [scope]: the window closed,
 * a quit shortcut, the system's quit ([SystemQuit]) or the process being told to stop. The first request starts the
 * guard and the others wait for the same run, so the note being typed is saved once, whichever came first.
 */
class WindowQuit(
    closeGuard: WindowCloseGuard,
    private val scope: CoroutineScope
) {
    private val guardRun = scope.async(start = CoroutineStart.LAZY) { closeGuard.beforeClose() }

    /** Runs the guard, unless it already ran, and then [then]. Any thread may ask. */
    fun request(then: () -> Unit) {
        scope.launch {
            guardRun.await()
            then()
        }
    }

    /**
     * For a shutdown hook: the JVM is ending and will not wait for anything else, so this blocks until the guard is
     * done, [SHUTDOWN_WAIT] at most; at once when it already ran, as after any other way of quitting.
     */
    fun finishBeforeShutdown() {
        runBlocking {
            guardRun.start()

            withTimeoutOrNull(timeout = SHUTDOWN_WAIT) { guardRun.join() }
        }
    }

    private companion object {
        val SHUTDOWN_WAIT = 4.seconds
    }
}
