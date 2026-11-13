package app.notekea.video.player

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import java.util.concurrent.CountDownLatch

/**
 * Runs work on macOS's main thread, the one AppKit wants windows made on. Java's `main` runs on another thread, and the
 * launcher keeps the main one in a run loop that serves the main dispatch queue: the work goes on that queue
 * (`dispatch_async_f`) and the caller waits until it ends. The YouTube window's web view crashed made anywhere else.
 */
internal object CocoaMainThread {
    private const val SYSTEM = "System"
    private const val MAIN_QUEUE = "_dispatch_main_q"
    private const val NO_CONTEXT = 0L

    @Suppress("FunctionName", "ktlint:standard:function-naming")
    private interface Dispatch : Library {
        // The context is a pointer, never used here: 0 is C's NULL on the 64-bit Macs Notekea runs on.
        fun dispatch_async_f(
            queue: Pointer,
            context: Long,
            work: Work
        )
    }

    private fun interface Work : Callback {
        fun invoke(context: Pointer?)
    }

    /** Does [work] on the main thread and returns once it has ended. */
    fun run(work: () -> Unit) {
        val done = CountDownLatch(1)
        var failure: Throwable? = null

        // Held until the main thread has run it: JNA frees a callback nothing refers to.
        val callback = Work {
            try {
                work()
            } catch (throwable: Throwable) {
                failure = throwable
            } finally {
                done.countDown()
            }
        }

        val mainQueue = NativeLibrary
            .getInstance(SYSTEM)
            .getGlobalVariableAddress(MAIN_QUEUE)

        Native
            .load(SYSTEM, Dispatch::class.java)
            .dispatch_async_f(
                queue = mainQueue,
                context = NO_CONTEXT,
                work = callback
            )

        done.await()

        failure?.let {
            throw it
        }
    }
}
