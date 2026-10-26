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
    private val shown = mutableListOf<RecordedCrash>()
    private val notice = object : CrashNotice {
        override fun show(
            crash: RecordedCrash
        ) {
            shown += crash
            events += NOTICE
        }
    }
    private val reporter = CrashReporter(
        recordCrash = RecordCrashUseCase(
            crashReportStore = store,
            systemEnvironment = FakeSystemEnvironment(operatingSystemName = "Linux", userHome = "/home/ana"),
            clock = FakeClock(currentTime = Instant.parse(input = "2026-10-26T10:15:30Z"))
        ),
        crashNotice = notice,
        later = { it() }
    )
    private val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

    @AfterTest
    fun tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(previousHandler)
    }

    @Test
    fun `a window crash is kept, shown, and only then the window closes, once`() {
        repeat(times = 2) {
            reporter.onWindowCrash(error = IllegalStateException("Layout failed"), threadName = UI_THREAD) {
                events += CLOSE
            }
        }

        assertEquals(
            expected = listOf(NOTICE, CLOSE) to 1,
            actual = events.toList() to store.reports.size
        )
        assertEquals(
            expected = FakeCrashReportStore.FOLDER + "crash-2026-10-26T10-15-30Z.txt",
            actual = shown.single().path
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
            expected = 1 to emptyList<String>(),
            actual = store.reports.size to events.toList()
        )
        assertTrue(actual = store.reports.values.single().contains(other = "Thread: $WORKER_THREAD"))
    }

    @Test
    fun `a report that cannot be written is still shown, so it can be copied`() {
        store.isBroken = true

        reporter.onWindowCrash(error = IllegalStateException("Disk full"), threadName = UI_THREAD) {}

        assertEquals(
            expected = null to true,
            actual = shown.single().let { it.path to it.text.contains(other = "Disk full") }
        )
    }

    companion object {
        private const val CLOSE = "close"
        private const val NOTICE = "notice"
        private const val UI_THREAD = "AWT-EventQueue-0"
        private const val WORKER_THREAD = "worker"
    }
}
