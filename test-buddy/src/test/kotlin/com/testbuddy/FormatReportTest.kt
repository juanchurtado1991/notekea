package com.testbuddy

import com.testbuddy.crash.AppEnvironment
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FormatReportTest {
    private val suite = sampleSuite {
        automated(id = "1.6-03", title = "Opening a note from the quick switcher") {}
        automated(id = "1.6-04", title = "Searching content") {}
        manual(id = "1.17-02", reason = ManualReason.EYES) {
            title(text = "Dark mode follows the system")
            steps(text = "Switch")
            expected(text = "Dark")
        }
    }
    private val environment =
        AppEnvironment(system = "Windows 11 10.0", java = "21.0.5", language = "es-CO", screen = "1920×1080 at 125%")

    @Test
    fun `a report lists the machine, the counts and every failure in English`() {
        val report = formatReport(
            suite = suite,
            environment = environment,
            automatic = listOf(
                CaseResult.Failed(
                    id = "1.6-03",
                    message = "expected to see \"Lisbon trip\", it did not appear",
                    duration = 2.minutes,
                    screenshot = Path.of("shots", "1.6-03.png"),
                    steps = listOf("Press Mod+P", "Type “lisbon”")
                ),
                CaseResult.Passed(id = "1.6-04", duration = 15.minutes),
                CaseResult.Skipped(id = "1.6-05")
            ),
            manual = listOf(
                ManualAnswer.Failed(
                    id = "1.17-02",
                    note = "the sidebar stayed white",
                    screenshot = "1.17-02.png",
                    steps = listOf("10:15:01 Workspace: OnSidePanelToggle")
                ),
                ManualAnswer.Passed(id = "1.17-03"),
                ManualAnswer.NotRun(id = "1.17-04")
            ),
            diagnostics = "last log line\n"
        )
        assertEquals(EXPECTED_REPORT, report)
    }

    @Test
    fun `a short run counts seconds, and no diagnostics leaves the section out`() {
        val report = formatReport(
            suite = suite,
            environment = environment,
            automatic = listOf(CaseResult.Passed(id = "1.6-04", duration = 42.seconds)),
            manual = emptyList()
        )
        assertTrue(report.contains("Automatic: 1 passed, 0 failed, 0 skipped (42 s)"))
        assertTrue(!report.contains("Diagnostics"))
    }

    @Test
    fun `the current machine is described`() {
        val current = AppEnvironment.current()
        assertTrue(current.system.isNotBlank() && current.language.isNotBlank() && current.screen.isNotBlank())
    }
}

private val EXPECTED_REPORT = """
    Sample QA report
    Sample 1.0
    OS: Windows 11 10.0
    Java: 21.0.5
    Language: es-CO
    Screen: 1920×1080 at 125%
    Automatic: 1 passed, 1 failed, 1 skipped (17 min)
      ❌ 1.6-03 Opening a note from the quick switcher: expected to see "Lisbon trip", it did not appear (screenshot: 1.6-03.png)
         Last steps of the test:
           Press Mod+P
           Type “lisbon”
    Manual: 1 passed, 1 failed, 1 not run
      ❌ 1.17-02 Dark mode follows the system: "the sidebar stayed white" (screenshot: 1.17-02.png)
         Last steps in Sample:
           10:15:01 Workspace: OnSidePanelToggle
    Diagnostics:
    last log line
""".trimIndent() + "\n"
