package com.testbuddy.app

import com.testbuddy.CaseResult
import com.testbuddy.ManualAnswer
import com.testbuddy.ManualReason
import com.testbuddy.crash.AppEnvironment
import com.testbuddy.crash.Breadcrumbs
import com.testbuddy.sampleSuite
import java.nio.file.Files
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ManagerSessionTest {
    private val root = Files.createTempDirectory("manager-session")
    private val folders = SelfTestFolders(root = root.resolve("Sample QA"))
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
            )
        )
        end()

        assertEquals(false, session.state.value.isRunning)
        assertEquals(session.state.value.results, session().state.value.results)
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
        assertEquals("Sample QA", SelfTestFolders.default(appName = "Sample").root.fileName.toString())
    }

    private var onEnd: () -> Unit = {}

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

    private fun end() = onEnd()
}
