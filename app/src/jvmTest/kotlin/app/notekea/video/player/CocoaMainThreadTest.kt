package app.notekea.video.player

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import org.junit.Assume.assumeTrue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The YouTube window's web view is made on macOS's main thread, as AppKit asks: made on Java's own it crashed at
 * once (SIGTRAP in `cocoa_wkwebview_engine::window_init`). macOS only, where that thread exists apart.
 */
class CocoaMainThreadTest {
    @Suppress("FunctionName", "ktlint:standard:function-naming")
    private interface Threads : Library {
        fun pthread_main_np(): Int
    }

    @Test
    fun `work given to the main thread runs there and the caller waits for it`() {
        assumeTrue(Platform.isMac())
        val threads = Native.load("System", Threads::class.java)
        val ran = mutableListOf<Int>()

        CocoaMainThread.run { ran += threads.pthread_main_np() }

        assertEquals(
            expected = listOf(MAIN, NOT_MAIN),
            actual = ran + threads.pthread_main_np()
        )
    }

    private companion object {
        const val MAIN = 1
        const val NOT_MAIN = 0
    }
}
