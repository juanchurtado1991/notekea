package com.testbuddy

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SuiteBuilderTest {
    @Test
    fun `the suite keeps its cases in order with what each declares`() = runTest {
        val suite = sampleSuite {
            automated(id = "1.1-01", title = "Opens") {}
            automated(id = "1.1-02", title = "Saves", timeout = 30.seconds) {}
            manual(id = "1.1-03", reason = ManualReason.EYES) {
                prepare("Personal Notes", "Work")
                title(text = plainText(english = "Looks right", "es" to "Se ve bien"))
                steps(text = plainText(english = "Open it"))
                expected(text = plainText(english = "It looks right"))
            }
        }
        assertEquals(listOf("1.1-01", "1.1-02"), suite.automated.map { it.id })
        assertEquals(listOf(2.minutes, 30.seconds), suite.automated.map { it.timeout })
        val manual = suite.manual.single()
        assertEquals(listOf("Personal Notes", "Work"), manual.data)
        assertEquals("Se ve bien", manual.title.resolve(language = "es"))
        assertEquals("Looks right", manual.title.resolve(language = "fr"))
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
            sampleSuite { manual(id = "1.1-03", reason = ManualReason.EYES) { title(text = plainText(english = "T")) } }
        }
        assertEquals("Manual case 1.1-03 has no steps", error.message)
    }

    @Test
    fun `a suite says how to launch the app`() {
        val error = assertFailsWith<IllegalArgumentException> { testBuddy(appName = "Lost", version = "1") {} }
        assertEquals("The suite of Lost does not say how to launch it", error.message)
    }

    @Test
    fun `nothing is backed up unless the suite says what`() {
        assertEquals(emptyList(), sampleSuite {}.backUp())
        val folder = sampleData()
        assertEquals(listOf(folder), sampleSuite { backUp { listOf(folder) } }.backUp())
    }

    @Test
    fun `manual reasons read as the case list writes them`() {
        assertEquals(listOf("eyes", "system", "packaged", "costly"), ManualReason.entries.map { it.label })
    }
}
