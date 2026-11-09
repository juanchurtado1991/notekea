package com.testbuddy

import androidx.compose.ui.input.key.Key
import com.testbuddy.ShortcutModifier.Mod
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class CaseScopeTest {
    private val workFolder = Files.createTempDirectory("case-scope-test")

    @Test
    fun `click, type and a shortcut drive the app and expectations see the result`() {
        val result = runCase {
            click(text = "Clicked 0 times")
            expectText(text = "Clicked 1 times")
            expectText(text = "Hello again")
            click(text = "first draft")
            type(text = "edited ")
            press(shortcut = Mod + Key.S)

            expectFile(
                path = "notes/draft.txt",
                containing = "edited"
            )
        }

        assertIs<CaseResult.Passed>(result)
    }

    @Test
    fun `a save made in the background is waited for`() {
        val result = runCase {
            click(text = "first draft")
            type(text = "later ")
            click(text = "Save later")

            expectFile(
                path = "notes/draft.txt",
                containing = "later"
            )
        }

        assertIs<CaseResult.Passed>(result)
    }

    @Test
    fun `an icon is clicked by its label, and a field is cleared`() {
        val result = runCase {
            clickIcon(label = "Add ten")
            expectText(text = "Clicked 10 times")
            click(text = "first draft")
            clearText()
            type(text = "fresh")
            press(shortcut = Mod + Key.S)

            expectFile(
                path = "notes/draft.txt",
                content = "fresh"
            )
        }

        assertIs<CaseResult.Passed>(result)

        val missing = assertIs<CaseResult.Failed>(runCase { clickIcon(label = "Nowhere") })

        assertEquals("expected a button labeled \"Nowhere\", it did not appear", missing.message)

        val nothing = assertIs<CaseResult.Failed>(runCase { clearText() })

        assertEquals("nothing was ready to clear: click a text field first", nothing.message)
    }

    @Test
    fun `a right click reaches the element's menu`() {
        assertIs<CaseResult.Passed>(
            runCase {
                rightClick(text = "Right-click me")
                expectText(text = "Menu open")
            }
        )
    }

    @Test
    fun `words inside a longer text are clicked where they are drawn`() {
        assertIs<CaseResult.Passed>(
            runCase {
                clickWords(words = "guide")
                expectText(text = "Guide opened")
            }
        )

        val stayed = assertIs<CaseResult.Failed>(
            runCase {
                expectNoText(
                    text = "need a precise",
                    substring = true
                )
            }
        )

        assertEquals("expected \"need a precise\" to go away, it stayed", stayed.message)
    }

    @Test
    fun `a text below the window is scrolled to before it is clicked`() {
        assertIs<CaseResult.Passed>(
            runCase {
                clickWords(words = "far below")
                expectText(text = "Far text clicked")
            }
        )
    }

    @Test
    fun `scrolling brings a far row into view`() {
        assertIs<CaseResult.Passed>(runCase { scrollTo(text = "Row 99") })
    }

    @Test
    fun `a text that never shows fails with a message a tester can read`() {
        val result = assertIs<CaseResult.Failed>(runCase { expectText(text = "Goodbye") })

        assertEquals("expected to see \"Goodbye\", it did not appear", result.message)
    }

    @Test
    fun `typing with no field focused says where to click`() {
        val result = assertIs<CaseResult.Failed>(runCase { type(text = "lost") })

        assertEquals(
            "nothing was ready to type \"lost\" in: click a text field first",
            result.message
        )
    }

    @Test
    fun `a text that stays fails expectNoText`() {
        val result = assertIs<CaseResult.Failed>(runCase { expectNoText(text = "Save later") })

        assertEquals("expected \"Save later\" to go away, it stayed", result.message)
    }

    @Test
    fun `a text that shows late gets past expectNoText but not expectTextStaysAway`() {
        assertIs<CaseResult.Passed>(
            runCase {
                click(text = "Fail later")
                expectNoText(text = "Something failed")
            }
        )

        val late = assertIs<CaseResult.Failed>(
            runCase {
                click(text = "Fail later")
                expectTextStaysAway(text = "Something failed")
            }
        )

        assertEquals("expected \"Something failed\" to stay away, it appeared", late.message)
    }

    @Test
    fun `a text that never shows passes expectTextStaysAway`() {
        assertIs<CaseResult.Passed>(
            runCase {
                expectTextStaysAway(
                    text = "Never",
                    settleMillis = 100
                )
            }
        )
    }

    @Test
    fun `a file changed by a late save fails expectFileStays, and one left alone passes`() {
        val changed = assertIs<CaseResult.Failed>(
            runCase {
                click(text = "first draft")
                type(text = "later ")
                click(text = "Save later")

                expectFileStays(
                    path = "notes/draft.txt",
                    content = "first draft"
                )
            }
        )

        assertEquals("expected \"notes/draft.txt\" to stay as it was, it changed", changed.message)

        val untouched = runCase {
            expectFileStays(
                path = "notes/draft.txt",
                content = "first draft",
                settleMillis = 100
            )
        }

        assertIs<CaseResult.Passed>(untouched)
    }

    @Test
    fun `settle lets the app run for the time it is given`() {
        val started = TimeSource.Monotonic.markNow()

        assertIs<CaseResult.Passed>(runCase { settle(millis = 200) })

        assertTrue(started.elapsedNow() >= 200.milliseconds)
    }

    @Test
    fun `file expectations say what the file holds instead`() {
        val wrong = assertIs<CaseResult.Failed>(
            runCase {
                expectFile(
                    path = "notes/draft.txt",
                    content = "other"
                )
            }
        )

        assertEquals(
            "expected \"notes/draft.txt\" to read \"other\", it reads \"first draft\"",
            wrong.message
        )

        val missing = assertIs<CaseResult.Failed>(runCase { expectFile(path = "notes/none.txt") })

        assertEquals("expected the file \"notes/none.txt\", it does not exist", missing.message)

        val there = assertIs<CaseResult.Failed>(runCase { expectNoFile(path = "notes/draft.txt") })

        assertEquals("expected no file \"notes/draft.txt\", it is there", there.message)

        assertIs<CaseResult.Passed>(runCase { expectNoFile(path = "notes/none.txt") })
    }

    @Test
    fun `a file outside the case's data cannot be reached`() {
        val result = assertIs<CaseResult.Failed>(runCase { file(path = "../../escape.txt") })

        assertEquals(
            "IllegalArgumentException: \"../../escape.txt\" is outside the case's data folder",
            result.message
        )
    }

    private fun runCase(body: CaseScope.() -> Unit): CaseResult {
        val suite = sampleSuite {
            automated(
                id = "T-1",
                title = "A case",
                body = body
            )
        }

        val runner = SuiteRunner(
            suite = suite,
            workFolder = workFolder,
            waitMillis = SHORT_WAIT_MILLIS
        )

        return runner.run(case = suite.automated.single())
    }

    private companion object {
        const val SHORT_WAIT_MILLIS = 2_000L
    }
}
