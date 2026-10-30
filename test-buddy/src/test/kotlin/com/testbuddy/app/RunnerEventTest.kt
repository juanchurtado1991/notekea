package com.testbuddy.app

import com.testbuddy.CaseResult
import com.testbuddy.sampleSuite
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds

class RunnerEventTest {
    @Test
    fun `every event reads back as it was written, tabs and line breaks included`() {
        val events = listOf(
            RunnerEvent.Started(id = "1.6-03"),
            RunnerEvent.Step(text = "Type “a\tb”"),
            RunnerEvent.Ended(result = CaseResult.Passed(id = "1.6-03", duration = 1_200.milliseconds)),
            RunnerEvent.Ended(result = CaseResult.Skipped(id = "1.6-04")),
            RunnerEvent.Ended(
                result = CaseResult.Failed(
                    id = "1.6-05",
                    message = "expected \"x\",\nit did not appear",
                    duration = 300.milliseconds,
                    screenshot = Path.of("shots", "1.6-05.png"),
                    steps = listOf("Click “Save”", "Expect “x”")
                )
            ),
            RunnerEvent.Done
        )

        assertEquals(events, events.map { RunnerEvent.parse(line = it.line()) })
        assertEquals(null, RunnerEvent.parse(line = "Info: (Vault) loaded 12 notes"))
    }

    @Test
    fun `the run in its own process tells each start, step and end, then that it is done`() {
        val suite = sampleSuite {
            automated(id = "1.1-01", title = "Shows the button") { expectText(text = "Save later") }
            automated(id = "1.1-02", title = "Shows a ghost") { expectText(text = "Ghost") }
        }
        val out = ByteArrayOutputStream()

        runAutomatedCases(suite = suite, workFolder = Files.createTempDirectory("runner"), out = PrintStream(out, true))

        val events = out.toString().lines().mapNotNull { RunnerEvent.parse(line = it) }
        assertEquals(RunnerEvent.Started(id = "1.1-01"), events.first())
        assertEquals(RunnerEvent.Step(text = "Expect “Save later”"), events[1])
        assertIs<CaseResult.Passed>((events[2] as RunnerEvent.Ended).result)
        assertEquals(listOf("Expect “Ghost”"), ((events[5] as RunnerEvent.Ended).result as CaseResult.Failed).steps)
        assertEquals(RunnerEvent.Done, events.last())
    }
}
