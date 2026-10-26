package app.notekea.core.crash.domain

import app.notekea.core.domain.FakeClock
import app.notekea.core.platform.domain.FakeSystemEnvironment
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class RecordCrashUseCaseTest {
    private val store = FakeCrashReportStore()
    private val recordCrash = RecordCrashUseCase(
        crashReportStore = store,
        systemEnvironment = FakeSystemEnvironment(operatingSystemName = "Linux", userHome = "/home/ana"),
        clock = FakeClock(currentTime = Instant.parse(input = "2026-10-26T10:15:30.123Z"))
    )

    @Test
    fun `a crash is kept with the versions, time, thread and the error with its cause`() = runTest {
        val error = IllegalStateException("Layout failed", IndexOutOfBoundsException("Range [55, 75)"))

        val crash = recordCrash(error = error, threadName = "AWT-EventQueue-0")
        val lines = crash.text.lines()

        assertEquals(
            expected = listOf(
                "Notekea crash report",
                "Version: 1.0.0",
                "Time: 2026-10-26T10:15:30Z",
                "OS: Linux 7.2",
                "Java: 21.0.11",
                "Thread: AWT-EventQueue-0",
                "",
                "java.lang.IllegalStateException: Layout failed"
            ),
            actual = lines.take(n = 8)
        )
        assertTrue(actual = lines.any { it.startsWith(prefix = "Caused by: java.lang.IndexOutOfBoundsException") })
        assertEquals(
            expected = FakeCrashReportStore.FOLDER + "crash-2026-10-26T10-15-30Z.txt" to crash.text,
            actual = crash.path to store.reports.getValue(key = "crash-2026-10-26T10-15-30Z.txt")
        )
    }

    @Test
    fun `long messages are cut, so a report never carries much of a note`() = runTest {
        val crash = recordCrash(error = IllegalArgumentException("x".repeat(n = 5_000)), threadName = "main")

        assertTrue(actual = crash.text.lines().all { it.length <= RecordCrashUseCase.MAX_LINE_LENGTH + 1 })
    }

    @Test
    fun `a report that cannot be written can still be copied`() = runTest {
        store.isBroken = true

        val crash = recordCrash(error = IllegalStateException("Disk is read-only"), threadName = "main")

        assertEquals(
            expected = null to true,
            actual = crash.path to crash.text.contains(other = "Disk is read-only")
        )
    }
}
