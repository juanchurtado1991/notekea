package com.testbuddy.app

import com.testbuddy.CaseResult
import com.testbuddy.ManualAnswer
import com.testbuddy.ManualReason
import com.testbuddy.ReportEnvironment

import com.testbuddy.sampleSuite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SelfTestSessionTest {
    private val root = Files.createTempDirectory("self-test-session")
    private val folders = SelfTestFolders(root = root.resolve("Sample Self-Test"))
    private val installed = root.resolve("installed-app").also { Files.createDirectories(it) }
    private val settings = installed.resolve("settings.txt").also { it.writeText("mine") }
    private val createdLater = root.resolve("created-later")
    private val suite = sampleSuite {
        automated(id = "1.1-01", title = "Shows the button") { expectText(text = "Save later") }
        automated(id = "1.1-02", title = "Shows a ghost") { expectText(text = "Ghost") }
        manual(id = "1.2-01", reason = ManualReason.EYES) { texts(title = "Looks right") }
        manual(id = "1.2-02", reason = ManualReason.SYSTEM) {
            prepare("notes")
            texts(title = "Opens the note")
        }
        manual(id = "1.2-03", reason = ManualReason.PACKAGED) { texts(title = "Installs") }
        backUp { listOf(installed, createdLater) }
    }

    @Test
    fun `the automatic part runs every case and is still there after reopening`() = runBlocking {
        val session = session()
        session.startAutomatic()
        val done = withTimeout(timeMillis = 60_000) { session.state.first { !it.isRunning && it.results.size == 2 } }
        assertIs<CaseResult.Passed>(done.results["1.1-01"])
        assertIs<CaseResult.Failed>(done.results["1.1-02"])
        assertEquals(null, done.runningId)
        assertEquals(done.results.keys, session().state.value.results.keys)
    }

    @Test
    fun `stopping skips the cases not run yet`() = runBlocking {
        lateinit var session: SelfTestSession
        val stopping = sampleSuite {
            automated(id = "A", title = "Stops") { session.stopAutomatic() }
            automated(id = "B", title = "Skipped") { error("must not run") }
        }
        session = SelfTestSession(suite = stopping, folders = folders, scope = this, background = Dispatchers.IO)
        session.startAutomatic()
        val done = withTimeout(timeMillis = 60_000) { session.state.first { !it.isRunning && it.results.size == 2 } }
        assertEquals(CaseResult.Skipped(id = "B"), done.results["B"])
    }

    @Test
    fun `the manual part backs up the app, prepares each case's data and keeps the answers`() = runBlocking {
        val session = session()
        session.goToManual()
        assertEquals(SelfTestStep.MANUAL, session.state.value.step)
        session.pass()
        assertEquals(1, session.state.value.manualIndex)
        assertEquals("first draft", folders.data.resolve("notes/draft.txt").readText())
        val picture = root.resolve("shot.png").also { it.writeText("png") }
        session.fail(note = " the note was empty \n", screenshot = picture)
        assertEquals("png", folders.report.resolve("1.2-02.png").readText())
        session.previous()
        assertEquals(1, session.state.value.manualIndex)
        val reopened = session()
        assertEquals(SelfTestStep.MANUAL, reopened.state.value.step)
        assertEquals(2, reopened.state.value.manualIndex)
        assertEquals(
            ManualAnswer.Failed(id = "1.2-02", note = "the note was empty", screenshot = "1.2-02.png"),
            reopened.state.value.answers["1.2-02"]
        )
    }

    @Test
    fun `after the last manual case the report lists what happened`() = runBlocking {
        val session = session()
        session.goToManual()
        session.pass()
        session.skip()
        session.fail(note = "it did not install", screenshot = null)
        val state = session.state.value
        assertEquals(SelfTestStep.REPORT, state.step)
        assertTrue(state.report.contains("Automatic: 0 passed, 0 failed, 2 skipped"))
        assertTrue(state.report.contains("Manual: 1 passed, 1 failed, 1 not run"))
        assertTrue(state.report.contains("❌ 1.2-03 Installs: \"it did not install\""))
        session.backToManual()
        assertEquals(SelfTestStep.MANUAL, session.state.value.step)
    }

    @Test
    fun `finishing puts the installed app back as it was and deletes the test data`() = runBlocking {
        val session = session()
        session.goToManual()
        settings.writeText("changed by the test")
        Files.createDirectories(createdLater)
        session.goToManual()
        session.finish()
        assertEquals("mine", settings.readText())
        assertFalse(createdLater.exists())
        assertFalse(folders.data.exists())
        assertTrue(session.state.value.isClean)
    }

    @Test
    fun `the default folder is in Documents when there is one`() {
        val folder = SelfTestFolders.default(appName = "Sample").root
        assertEquals("Sample Self-Test", folder.fileName.toString())
    }

    private fun kotlinx.coroutines.CoroutineScope.session(): SelfTestSession = SelfTestSession(
        suite = suite,
        folders = folders,
        scope = this,
        background = Dispatchers.IO,
        environment = { ReportEnvironment(system = "Test OS", language = "en", screen = "no screen") }
    )

    private fun session(): SelfTestSession = runBlocking { session() }
}
