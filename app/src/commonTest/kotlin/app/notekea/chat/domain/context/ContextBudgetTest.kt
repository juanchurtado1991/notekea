package app.notekea.chat.domain.context

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.llm.domain.LlmModelInfo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What a turn's prompt holds, counted in tokens (`docs/agent-loop.md`, step 2). The fake model counts four
 * characters a token, so an entry of 400 characters is 100 tokens.
 */
class ContextBudgetTest {
    private val engine = FakeLlmEngine()
    private val entries = (1..10).map {
        "$it".padEnd(
            length = 400,
            padChar = 'x'
        )
    }

    private fun budget(tokens: Int) = ContextBudget(
        llmEngine = engine,
        tokens = tokens,
        fixed = "The task.",
        replyTokens = 100
    )

    @Test
    fun `everything is kept while it fits`() = runTest {
        assertEquals(
            expected = entries,
            actual = budget(tokens = 2_000).fit(entries = entries).entries
        )
    }

    @Test
    fun `the oldest go first, whole and several at once, with a line saying so, and the newest always stays`() =
        runTest {
            val fitted = budget(tokens = 710).fit(entries = entries).entries

            val alone = budget(tokens = 150).fit(entries = entries).entries

            assertEquals(
                expected = listOf(
                    listOf(ContextBudget.droppedLine(count = 6)) + entries.takeLast(n = 4),
                    listOf(ContextBudget.droppedLine(count = 9), entries.last())
                ),
                actual = listOf(fitted, alone)
            )
        }

    @Test
    fun `what was dropped stays dropped, so the prompt's start does not change at every move`() =
        runTest {
            val budget = budget(tokens = 710)
            val first = budget.fit(entries = entries)

            val next = budget.fit(
                entries = entries + "11".padEnd(
                    length = 400,
                    padChar = 'x'
                ),
                droppedBefore = first.dropped
            )

            assertEquals(
                expected = first.entries.take(n = 5),
                actual = next.entries.take(n = 5)
            )
        }

    @Test
    fun `a model's profile gives the loop its tokens, never more than is read in some seconds`() =
        runTest {
            engine.readiness = Outcome.Success(
                value = LlmModelInfo(
                    isSmall = false,
                    contextTokens = 16_384
                )
            )

            val wide = ModelProfile.of(llmEngine = engine)
            engine.readiness = Outcome.Failure(error = LlmError.NoModel)

            assertEquals(
                expected = listOf(16_384 to 8_192, 4_096 to 4_096),
                actual = listOf(
                    wide.contextTokens to wide.loopTokens,
                    ModelProfile.of(llmEngine = engine).let {
                        it.contextTokens to it.loopTokens
                    }
                )
            )
        }
}
