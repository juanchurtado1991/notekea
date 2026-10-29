package com.testbuddy

import androidx.compose.ui.test.onRoot
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SuiteRunnerTest {
    private val workFolder = Files.createTempDirectory("suite-runner-test")

    @Test
    fun `each case gets fresh copies of the data and its own app data folder, deleted after it`() {
        val seen = mutableListOf<String>()
        val suite = sampleSuite {
            automated(id = "A", title = "Changes the note") {
                val original = sampleData().resolve("notes/draft.txt")
                val copied = Files.getLastModifiedTime(file(path = "notes/draft.txt")).toMillis()
                seen += (copied == Files.getLastModifiedTime(original).toMillis()).toString()
                seen += file(path = "notes/draft.txt").readText()
                file(path = "notes/draft.txt").writeText("changed")
                folders.appData.resolve("settings.txt").writeText("A")
            }
            automated(id = "B", title = "Sees the original") {
                seen += file(path = "notes/draft.txt").readText()
                seen += folders.appData.listDirectoryEntries().size.toString()
            }
        }
        val results = runner(suite = suite).run()
        assertTrue(results.all { it is CaseResult.Passed })
        assertEquals(listOf("true", "first draft", "first draft", "0"), seen)
        assertFalse(workFolder.resolve("cases/A").exists())
        assertEquals("first draft", sampleData().resolve("notes/draft.txt").readText())
    }

    @Test
    fun `a failure does not stop the run and keeps a screenshot`() {
        val suite = sampleSuite {
            automated(id = "1.2-03", title = "Fails") { expectText(text = "Nowhere") }
            automated(id = "1.2-04", title = "Passes") { expectText(text = "Save later") }
        }
        val reported = mutableListOf<String>()
        val results = runner(suite = suite).run { reported += it.id }
        val failed = assertIs<CaseResult.Failed>(results[0])
        assertIs<CaseResult.Passed>(results[1])
        assertEquals(listOf("1.2-03", "1.2-04"), reported)
        val screenshot = assertNotNull(failed.screenshot)
        assertEquals("1.2-03.png", screenshot.fileName.toString())
        assertTrue(Files.size(screenshot) > 0)
    }

    @Test
    fun `a case with a window of its own runs at that size`() {
        var width = 0f
        val suite = sampleSuite {
            automated(id = "W", title = "Narrow", window = WindowSize(width = 320, height = 480)) {
                width = ui.onRoot().fetchSemanticsNode().size.width.toFloat()
            }
        }
        assertIs<CaseResult.Passed>(runner(suite = suite).run().single())
        assertEquals(320f, width)
    }

    @Test
    fun `an unexpected error is reported with its type`() {
        val suite = sampleSuite { automated(id = "E", title = "Throws") { error("the disk is full") } }
        val failed = assertIs<CaseResult.Failed>(runner(suite = suite).run().single())
        assertEquals("IllegalStateException: the disk is full", failed.message)
    }

    @Test
    fun `once stopped, the remaining cases are skipped`() {
        var stop = false
        val suite = sampleSuite {
            automated(id = "1", title = "Runs") { stop = true }
            automated(id = "2", title = "Skipped") { error("must not run") }
        }
        val results = runner(suite = suite).run(shouldStop = { stop })
        assertIs<CaseResult.Passed>(results[0])
        assertEquals(CaseResult.Skipped(id = "2"), results[1])
    }

    private fun runner(suite: Suite) = SuiteRunner(suite = suite, workFolder = workFolder, waitMillis = 2_000L)
}
