package com.testbuddy.crash

import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CrashBuddyTest {
    private val events = mutableListOf<String>()
    private val reports = mutableListOf<String>()
    private val fallbackShown = mutableListOf<CrashReport>()
    private var isStoreBroken = false
    private val crashBuddy = CrashBuddy(
        config = CrashConfig(appName = "Sample", version = "1.0", reportFolder = Path.of("/tmp/never")),
        record = { error, thread ->
            if (isStoreBroken) error("disk full")
            reports += "$thread: ${error.message}"
            CrashReport(text = "report of ${error.message}", path = "/reports/crash.txt")
        },
        fallback = {
            fallbackShown += it
            events += FALLBACK
        },
        later = { it() }
    )
    private val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

    @AfterTest
    fun tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(previousHandler)
    }

    @Test
    fun `a window crash is kept and published, and the window closes only when the notice is closed`() {
        crashWindow(window = MAIN_WINDOW)
        val beforeClosing = events.toList()

        crashBuddy.close()
        crashBuddy.close()

        assertEquals(emptyList(), beforeClosing)
        assertEquals(listOf(CLOSE), events)
        assertEquals("/reports/crash.txt", crashBuddy.crash.value?.path)
    }

    @Test
    fun `the failed window failing again on every frame changes nothing`() {
        repeat(times = 3) { crashWindow(window = MAIN_WINDOW) }

        assertEquals(emptyList<String>() to 1, events.toList() to reports.size)
    }

    @Test
    fun `when the notice itself fails, the swing fallback says the same and the app closes`() {
        crashWindow(window = MAIN_WINDOW)
        crashWindow(window = NOTICE_WINDOW)
        crashWindow(window = NOTICE_WINDOW)

        assertEquals(listOf(FALLBACK, CLOSE) to crashBuddy.crash.value, events.toList() to fallbackShown.single())
    }

    @Test
    fun `an error on another thread is kept without a notice`() {
        crashBuddy.installThreadHandler()

        Thread({ throw IllegalStateException("Background failure") }, WORKER_THREAD).apply {
            start()
            join()
        }

        assertEquals(listOf("$WORKER_THREAD: Background failure"), reports)
        assertEquals(null, crashBuddy.crash.value)
    }

    @Test
    fun `a report that cannot be kept is still published, so it can be copied`() {
        isStoreBroken = true

        crashWindow(window = MAIN_WINDOW)

        assertEquals(null to true, crashBuddy.crash.value?.let { it.path to it.text.contains("Layout failed") })
    }

    private fun crashWindow(window: String) {
        crashBuddy.onWindowCrash(error = IllegalStateException("Layout failed"), threadName = "AWT", window = window) {
            events += CLOSE
        }
    }

    private companion object {
        const val CLOSE = "close"
        const val FALLBACK = "fallback"
        const val MAIN_WINDOW = "main"
        const val NOTICE_WINDOW = "notice"
        const val WORKER_THREAD = "worker"
    }
}
