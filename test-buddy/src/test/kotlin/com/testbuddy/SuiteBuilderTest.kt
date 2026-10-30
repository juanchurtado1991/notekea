package com.testbuddy

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SuiteBuilderTest {
    @Test
    fun `the suite keeps its cases in order with what each declares`() {
        val suite = sampleSuite {
            automated(id = "1.1-01", title = "Opens") {}
            automated(
                id = "1.1-02",
                title = "Saves",
                timeout = 30.seconds,
                window = WindowSize(width = 400, height = 300)
            ) {
            }
            manual(id = "1.1-03", reason = ManualReason.EYES) {
                prepare("Personal Notes", "Work")
                title(text = "Looks right")
                steps(text = "Open it")
                expected(text = "It looks right")
            }
        }
        assertEquals(listOf("1.1-01", "1.1-02"), suite.automated.map { it.id })
        assertEquals(listOf(2.minutes, 30.seconds), suite.automated.map { it.timeout })
        assertEquals(listOf(null, WindowSize(width = 400, height = 300)), suite.automated.map { it.window })
        val manual = suite.manual.single()
        assertEquals(listOf("Personal Notes", "Work"), manual.data)
        assertEquals(
            listOf("Looks right", "Open it", "It looks right"),
            listOf(manual.title, manual.steps, manual.expected)
        )
        assertEquals("Sample", suite.appName)
        assertEquals(WindowSize(width = 640, height = 480), suite.window)
    }

    @Test
    fun `an ID is used once, across automated and manual cases`() {
        val error = assertFailsWith<IllegalArgumentException> {
            sampleSuite {
                automated(id = "1.1-01", title = "Opens") {}
                manual(id = "1.1-01", reason = ManualReason.SYSTEM) {}
            }
        }
        assertEquals("Two cases use the ID 1.1-01", error.message)
    }

    @Test
    fun `an ID has no spaces`() {
        val error = assertFailsWith<IllegalArgumentException> {
            sampleSuite { automated(id = "1.1 01", title = "Opens") {} }
        }
        assertEquals("A case ID has no spaces: \"1.1 01\"", error.message)
    }

    @Test
    fun `a manual case needs its three texts`() {
        val error = assertFailsWith<IllegalArgumentException> {
            sampleSuite { manual(id = "1.1-03", reason = ManualReason.EYES) { title(text = "T") } }
        }
        assertEquals("Manual case 1.1-03 has no steps", error.message)
    }

    @Test
    fun `a suite says how to launch the app`() {
        val error = assertFailsWith<IllegalArgumentException> { testBuddy(appName = "Lost", version = "1") {} }
        assertEquals("The suite of Lost does not say how to launch it", error.message)
    }

    @Test
    fun `a manual case answers Passed or Failed unless it names its own answers, and can set a variable`() {
        val suite = sampleSuite {
            manual(id = "G-01", reason = ManualReason.PACKAGED) {
                title(text = "T")
                steps(text = "S")
                expected(text = "E")
            }
            manual(id = "G-09", reason = ManualReason.EYES) {
                title(text = "T")
                steps(text = "S")
                expected(text = "E")
                answers(passed = "Nothing odd", failed = "I found something")
                launchWith(name = "SAMPLE_CRASH_TEST", value = "1")
            }
        }
        val (plain, freeUse) = suite.manual
        assertEquals(listOf("Passed", "Failed"), listOf(plain.passLabel, plain.failLabel))
        assertEquals(emptyMap(), plain.launchEnvironment)
        assertEquals(listOf("Nothing odd", "I found something"), listOf(freeUse.passLabel, freeUse.failLabel))
        assertEquals(mapOf("SAMPLE_CRASH_TEST" to "1"), freeUse.launchEnvironment)
    }

    @Test
    fun `the report's diagnostics come from the suite, none unless it says`() {
        assertEquals(null, sampleSuite {}.diagnostics())
        val suite = sampleSuite { diagnostics { "Recent log: saved" } }
        assertEquals("Recent log: saved", suite.shard(index = 0, count = 2).diagnostics())
    }

    @Test
    fun `manual reasons read as the case list writes them`() {
        assertEquals(listOf("eyes", "system", "packaged", "costly"), ManualReason.entries.map { it.label })
    }
}
