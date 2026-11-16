package app.notekea.chat.domain.context

import app.notekea.core.llm.domain.FakeLlmEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The turn's steps kept aside (`docs/agent-step-memory.md`): from the web's evaluation of 2026-11-16, three pages on
 * memorizing read first were dropped for the twelve note sections read after them, and the answer found nothing.
 */
class StepMemoryBudgetTest {
    private val engine = FakeLlmEngine()
    private val memory = FakeStepMemory()

    private val page =
        "fetch \"https://athenify.io/es\":\nMemorizing\n\nSpaced repetition helps memorizing."

    private val sections = (1..9).map {
        "read \"Applied statistics\" section $it:\n\n".padEnd(
            length = 400,
            padChar = 'x'
        )
    }

    private fun budget(memory: StepMemory?) = ContextBudget(
        llmEngine = engine,
        tokens = 710,
        fixed = "The task.",
        replyTokens = 100,
        memory = memory,
        need = "the best ways of memorizing, with sources",
        space = "turn"
    )

    @Test
    fun `a page dropped for the notes read after it comes back in the part the task needs`() =
        runTest {
            val fitted = budget(memory = memory).fit(entries = listOf(page) + sections)

            val recalled = fitted.entries[1]

            assertTrue(actual = recalled.startsWith(prefix = ContextBudget.RECALLED))

            assertEquals(
                expected = listOf(true, true),
                actual = listOf(
                    "Spaced repetition helps memorizing." in recalled,
                    "From step 1, fetch \"https://athenify.io/es\":" in recalled
                )
            )
        }

    @Test
    fun `with no memory, or nothing the task needs, what was dropped is only said`() = runTest {
        val alone = budget(memory = null).fit(entries = listOf(page) + sections)
        val unneeded = budget(memory = memory).fit(entries = sections + sections)

        assertEquals(
            expected = listOf(false, false),
            actual = listOf(alone, unneeded).map { fitted ->
                fitted.entries.any { it.startsWith(prefix = ContextBudget.RECALLED) }
            }
        )
    }

    @Test
    fun `what a turn kept is forgotten when it ends`() = runTest {
        val budget = budget(memory = memory)
        budget.fit(entries = listOf(page) + sections)
        budget.forget()

        assertEquals(
            expected = emptyMap(),
            actual = memory.kept
        )
    }

    @Test
    fun `the model asks back what it needs from the results no longer shown`() = runTest {
        val budget = budget(memory = memory)
        val entries = listOf(page) + sections
        val dropped = budget.fit(entries = entries).dropped

        val asked = budget.recalled(
            about = "spaced repetition",
            entries = entries,
            dropped = dropped
        )

        val missing = budget.recalled(
            about = "arepas",
            entries = entries,
            dropped = dropped
        )

        assertEquals(
            expected = listOf(true, true, false),
            actual = listOf(
                asked.orEmpty().contains(other = "Spaced repetition helps memorizing."),
                missing.orEmpty().contains(other = "nothing about it"),
                budget.recalled(
                    about = "spaced repetition",
                    entries = entries,
                    dropped = 0
                ) != null
            )
        )
    }

    @Test
    fun `the line said for what was dropped tells to recall it when it is kept`() = runTest {
        val line = budget(memory = memory)
            .fit(entries = listOf(page) + sections)
            .entries
            .first()

        assertTrue(actual = "recall what you need" in line)
    }

    @Test
    fun `a result is cut into its first line and its paragraphs, a long one at the most`() {
        val long = "b".repeat(n = 900)

        assertEquals(
            expected = listOf(
                "read \"Ideas\":",
                "First.\n\nSecond.",
                "b".repeat(n = 700),
                "b".repeat(n = 200)
            ),
            actual = StepChunks.of(entry = "read \"Ideas\":\nFirst.\n\nSecond.\n\n$long")
        )
    }
}
