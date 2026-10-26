package app.notekea.core.crash.presentation

import app.notekea.core.crash.domain.FakeCrashReportStore
import app.notekea.core.crash.domain.RecordCrashUseCase
import app.notekea.core.crash.domain.RecordedCrash
import app.notekea.core.domain.FakeClock
import app.notekea.core.platform.domain.FakeSystemEnvironment
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class CrashReporterTest {
    private val store = FakeCrashReportStore()
    private val events = mutableListOf<String>()
    private val fallbackShown = mutableListOf<RecordedCrash>()
    private val fallbackNotice = object : CrashNotice {
        override fun show(
            crash: RecordedCrash
        ) {
            fallbackShown += crash
            events += FALLBACK
        }
    }
    private val reporter = CrashReporter(
        recordCrash = RecordCrashUseCase(
            crashReportStore = store,
            systemEnvironment = FakeSystemEnvironment(operatingSystemName = "Linux", userHome = "/home/ana"),
            clock = FakeClock(currentTime = Instant.parse(input = "2026-10-26T10:15:30Z"))
        ),
        fallbackNotice = fallbackNotice,
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

        reporter.close()
        reporter.close()

        assertEquals(
            expected = Triple(emptyList(), listOf(CLOSE), FakeCrashReportStore.FOLDER + REPORT_NAME),
            actual = Triple(beforeClosing, events.toList(), reporter.crash.value?.path)
        )
    }

    @Test
    fun `the failed window failing again on every frame changes nothing`() {
        repeat(times = 3) { crashWindow(window = MAIN_WINDOW) }

        assertEquals(
            expected = emptyList<String>() to 1,
            actual = events.toList() to store.reports.size
        )
    }

    @Test
    fun `when the notice itself fails, the swing fallback says the same and the app closes`() {
        crashWindow(window = MAIN_WINDOW)
        crashWindow(window = NOTICE_WINDOW)
        crashWindow(window = NOTICE_WINDOW)

        assertEquals(
            expected = listOf(FALLBACK, CLOSE) to reporter.crash.value,
            actual = events.toList() to fallbackShown.single()
        )
    }

    @Test
    fun `an error on another thread is kept without a notice`() {
        reporter.installThreadHandler()

        Thread({ throw IllegalStateException("Background failure") }, WORKER_THREAD).apply {
            start()
            join()
        }

        assertEquals(
            expected = Triple(1, null, emptyList<String>()),
            actual = Triple(store.reports.size, reporter.crash.value, events.toList())
        )
        assertTrue(actual = store.reports.values.single().contains(other = "Thread: $WORKER_THREAD"))
    }

    @Test
    fun `a report that cannot be written is still published, so it can be copied`() {
        store.isBroken = true

        crashWindow(window = MAIN_WINDOW)

        assertEquals(
            expected = null to true,
            actual = reporter.crash.value?.let { it.path to it.text.contains(other = "Layout failed") }
        )
    }

    private fun crashWindow(
        window: String
    ) {
        reporter.onWindowCrash(
            error = IllegalStateException("Layout failed"),
            threadName = UI_THREAD,
            window = window
        ) {
            events += CLOSE
        }
    }

    companion object {
        private const val CLOSE = "close"
        private const val FALLBACK = "fallback"
        private const val MAIN_WINDOW = "main"
        private const val NOTICE_WINDOW = "notice"
        private const val REPORT_NAME = "crash-2026-10-26T10-15-30Z.txt"
        private const val UI_THREAD = "AWT-EventQueue-0"
        private const val WORKER_THREAD = "worker"
    }
}
