package com.testbuddy.app

import com.testbuddy.CaseResult
import com.testbuddy.ManualAnswer
import com.testbuddy.ManualReason
import com.testbuddy.crash.AppEnvironment
import com.testbuddy.crash.Breadcrumbs
import com.testbuddy.sampleSuite
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ManagerSessionTest {
    private val root = Files.createTempDirectory("manager-session")
    private val folders = AutoTestFolders(root = root.resolve("Sample QA"))
    private val suite = sampleSuite {
        automated(id = "1.1-01", title = "Shows the button") {}
        automated(id = "1.1-02", title = "Shows a ghost") {}
        manual(id = "1.2-01", reason = ManualReason.EYES) {
            title(text = "Looks right")
            steps(text = "1. Open the app.\n2. Look at it,\n   closely.")
            expected(text = "It looks right")
        }
        manual(id = "1.2-02", reason = ManualReason.SYSTEM) {
            prepare("notes")
            title(text = "Opens the note")
            steps(text = "Open the note")
            expected(text = "It opens")
            launchWith(name = "SAMPLE_CRASH_TEST", value = "1")
        }
    }
    private val events = mutableListOf<RunnerEvent>()

    @AfterTest
    fun tearDown() {
        Breadcrumbs.clear()
        root.toFile().deleteRecursively()
    }

    @Test
    fun `a run shows its case and steps as they come, and keeps the results`() {
        val session = session()
        session.startAutomatic()
        send(session, RunnerEvent.Started(id = "1.1-01"), RunnerEvent.Step(text = "Click “Save”"))
        assertEquals("1.1-01" to listOf("Click “Save”"), session.state.value.let { it.runningId to it.runningSteps })
        assertTrue(session.state.value.isRunning)

        send(
            session,
            RunnerEvent.Ended(result = CaseResult.Passed(id = "1.1-01", duration = 2.seconds)),
            RunnerEvent.Ended(
                result = CaseResult.Failed(
                    id = "1.1-02",
                    message = "no ghost",
                    duration = 1.seconds,
                    steps = listOf("Expect “Ghost”")
                )
            ),
            RunnerEvent.Done
        )
        end()

        assertEquals(false, session.state.value.isRunning)
        assertEquals(session.state.value.results, session().state.value.results)
    }

    @Test
    fun `a process that dies fails the case it was running and the report counts the rest as skipped`() {
        val session = session()
        session.startAutomatic()
        send(session, RunnerEvent.Started(id = "1.1-01"), RunnerEvent.Step(text = "Click “Save”"))
        end(exitCode = 137)

        val failed = assertIs<CaseResult.Failed>(session.state.value.results["1.1-01"])
        assertEquals("the test process stopped (exit code 137)", failed.message)
        assertEquals(listOf("Click “Save”"), failed.steps)
        assertEquals(false, session.state.value.isRunning)
        session.select(tab = ManagerTab.REPORT)
        assertTrue(session.state.value.report.contains("Automatic: 0 passed, 1 failed, 1 skipped"))
    }

    @Test
    fun `a process that dies between two cases fails the next one`() {
        val session = session()
        session.startAutomatic()
        send(
            session,
            RunnerEvent.Started(id = "1.1-01"),
            RunnerEvent.Ended(result = CaseResult.Passed(id = "1.1-01", duration = 1.seconds))
        )
        end(exitCode = 1)

        assertIs<CaseResult.Passed>(session.state.value.results["1.1-01"])
        assertIs<CaseResult.Failed>(session.state.value.results["1.1-02"])
    }

    @Test
    fun `a process that quits without saying Done is a failure even with exit code 0`() {
        val session = session()
        session.startAutomatic()
        send(session, RunnerEvent.Started(id = "1.1-01"))
        end(exitCode = 0)

        assertIs<CaseResult.Failed>(session.state.value.results["1.1-01"])
    }

    @Test
    fun `a run the tester stopped fails nothing`() {
        val session = session()
        session.startAutomatic()
        send(session, RunnerEvent.Started(id = "1.1-01"))
        session.stopAutomatic()
        end(exitCode = 143)

        assertEquals(emptyMap(), session.state.value.results)
        assertEquals(false, session.state.value.isRunning)
    }

    @Test
    fun `a run that says Done and exits fine fails nothing`() {
        val session = session()
        session.startAutomatic()
        send(session, RunnerEvent.Done)
        end(exitCode = 0)

        assertEquals(emptyMap(), session.state.value.results)
    }

    @Test
    fun `the run tells the exit code of its process and keeps the process output in the log`() {
        val java = Path.of(System.getProperty("java.home"), "bin", "java").toString()
        val exitCode = CompletableFuture<Int>()
        val log = root.resolve("run.txt")
        AutomaticRun(
            log = log,
            onEvent = {},
            onEnd = { exitCode.complete(it) },
            startProcess = { ProcessBuilder(java, "--no-such-option").redirectErrorStream(true).start() }
        ).start()

        assertNotEquals(0, exitCode.get(1, TimeUnit.MINUTES))
        assertTrue(log.readText().isNotBlank())
    }

    @Test
    fun `a manual case goes step by step to its result, and the tester's place is kept`() {
        val session = session()
        session.select(tab = ManagerTab.MANUAL)
        assertEquals(listOf("Open the app.", "Look at it, closely."), suite.manual.first().stepList)
        session.nextStep()
        session.nextStep()
        session.nextStep()
        assertEquals(2, session.state.value.manualStep)
        session.pass()
        assertEquals(1 to 0, session.state.value.let { it.manualIndex to it.manualStep })
        assertEquals("first draft", folders.data.resolve("notes/draft.txt").readText())
        assertEquals(mapOf("SAMPLE_CRASH_TEST" to "1"), session.launchEnvironment())

        val reopened = session()
        assertEquals(
            Triple(ManagerTab.MANUAL, 1, 0),
            reopened.state.value.let {
                Triple(it.tab, it.manualIndex, it.manualStep)
            }
        )
        assertEquals(ManualAnswer.Passed(id = "1.2-01"), reopened.state.value.answers["1.2-01"])
    }

    @Test
    fun `a failed manual case keeps the note, the screenshot and the tester's last steps for the report`() {
        val session = session()
        session.select(tab = ManagerTab.MANUAL)
        session.skip()
        Breadcrumbs.record(step = "Editor: OnOpen")
        val picture = root.resolve("shot.png").also { it.writeText("png") }

        session.fail(note = " the note was empty \n", screenshot = picture)

        assertEquals(ManagerTab.REPORT, session.state.value.tab)
        assertEquals("png", folders.report.resolve("1.2-02.png").readText())
        val report = folders.report.resolve("report.txt").readText()
        assertTrue(report.contains("❌ 1.2-02 Opens the note: \"the note was empty\" (screenshot: 1.2-02.png)"))
        assertTrue(report.contains("Last steps in Sample:"))
        assertTrue(report.contains(" Editor: OnOpen"))
        assertTrue(report.contains("Manual: 0 passed, 1 failed, 1 not run"))
        assertEquals(report, session.state.value.report)
    }

    @Test
    fun `the default folder is named after the app's QA build`() {
        assertEquals("Sample QA", AutoTestFolders.default(appName = "Sample").root.fileName.toString())
    }

    private var onEnd: (Int) -> Unit = {}

    private fun session() = ManagerSession(
        suite = suite,
        folders = folders,
        environment = { AppEnvironment(system = "Test OS", java = "21", language = "en", screen = "no screen") },
        startRun = { onEvent, end ->
            onEnd = end
            events.clear()
            AutomaticRun(log = root.resolve("log.txt"), onEvent = onEvent, onEnd = end).also { sink = onEvent }
        }
    )

    private var sink: (RunnerEvent) -> Unit = {}

    private fun send(
        session: ManagerSession,
        vararg sent: RunnerEvent
    ) {
        check(session.state.value.isRunning)
        sent.forEach(sink)
    }

    private fun end(exitCode: Int = 0) = onEnd(exitCode)
}
