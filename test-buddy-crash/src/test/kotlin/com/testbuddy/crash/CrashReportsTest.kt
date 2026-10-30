package com.testbuddy.crash

import java.nio.file.Files
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrashReportsTest {
    private val folder = Files.createTempDirectory("crash-reports")
    private val config =
        CrashConfig(appName = "Sample", version = "1.2 (m1-rc1)", reportFolder = folder.resolve("crashes"))
    private val environment =
        AppEnvironment(system = "Linux 6.8", java = "21.0.5", language = "es-CO", screen = "1920×1080 at 125%")

    @AfterTest
    fun tearDown() {
        folder.toFile().deleteRecursively()
    }

    @Test
    fun `a report says the app, when, where, the last steps and the error`() {
        val report =
            record(error = IllegalStateException("Layout failed"), steps = listOf("10:15:01 Editor: save shortcut"))

        val lines = report.text.lines()
        assertEquals(
            listOf(
                "Sample crash report", "Version: 1.2 (m1-rc1)", "Time: 2026-10-26T10:15:30Z", "OS: Linux 6.8",
                "Java: 21.0.5", "Language: es-CO", "Screen: 1920×1080 at 125%", "Thread: AWT", "", "Last steps:",
                "10:15:01 Editor: save shortcut", "", "java.lang.IllegalStateException: Layout failed"
            ),
            lines.take(n = 13)
        )
        assertEquals(report.text, Files.readString(folder.resolve("crashes/crash-2026-10-26T10-15-30Z.txt")))
    }

    @Test
    fun `long lines are cut and no steps say so`() {
        val report = record(error = IllegalStateException("x".repeat(n = 1_000)), steps = emptyList())

        assertTrue("(none recorded)" in report.text.lines())
        assertTrue(report.text.lines().all { it.length <= CrashReports.MAX_LINE_LENGTH + 1 })
    }

    @Test
    fun `only the newest reports stay`() {
        repeat(times = CrashReports.KEPT + 3) { second ->
            record(
                error = IllegalStateException("crash $second"),
                steps = emptyList(),
                now = Instant.parse("2026-10-26T10:15:${10 + second}Z")
            )
        }

        val names = Files.list(folder.resolve("crashes")).use { files ->
            files.map { it.fileName.toString() }.sorted().toList()
        }
        assertEquals(CrashReports.KEPT, names.size)
        assertEquals("crash-2026-10-26T10-15-13Z.txt", names.first())
    }

    @Test
    fun `a report that cannot be written has no path but its text`() {
        Files.writeString(folder.resolve("crashes"), "a file where the folder should be")

        val report = record(error = IllegalStateException("Layout failed"), steps = emptyList())

        assertEquals(null, report.path)
        assertTrue(report.text.contains("Layout failed"))
    }

    private fun record(
        error: Throwable,
        steps: List<String>,
        now: Instant = Instant.parse("2026-10-26T10:15:30Z")
    ) = CrashReports.record(
        config = config,
        error = error,
        threadName = "AWT",
        now = now,
        environment = environment,
        steps = steps
    )
}
