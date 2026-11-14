package app.notekea.chat.domain.context

import app.notekea.core.llm.domain.FakeLlmEngine
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The trimming of [ContextBudget] over many turns made at random (`docs/agent-loop.md`, step 2): whatever was found
 * and however little room there is, what holds for every fit of every turn.
 */
class ContextBudgetPropertyTest {
    private val engine = FakeLlmEngine()

    // One turn: results of any size found one after another, fitted before each move as the agent does.
    private suspend fun turn(random: Random): List<String> {
        val tokens = random.nextInt(
            from = 200,
            until = 4_000
        )

        val fixed = "x".repeat(n = random.nextInt(until = 200))

        val budget = ContextBudget(
            llmEngine = engine,
            tokens = tokens,
            fixed = fixed,
            replyTokens = REPLY_TOKENS
        )

        val room = tokens - engine.countTokens(text = fixed) - REPLY_TOKENS
        val found = mutableListOf<String>()
        val broken = mutableListOf<String>()
        var dropped = 0

        repeat(
            times = random.nextInt(
                from = 1,
                until = 30
            )
        ) { number ->
            found += "$number ".padEnd(
                length = random.nextInt(
                    from = 8,
                    until = 3_000
                ),
                padChar = 'x'
            )

            val fitted = budget.fit(found, dropped)
            val kept = found.drop(n = fitted.dropped)
            val size = fitted.entries.sumOf { engine.countTokens(text = it) }
            val isAlone = kept.size == 1

            if (fitted.dropped < dropped) broken += "what was dropped came back"
            if (kept.lastOrNull() != found.last()) broken += "the newest was dropped"
            if (fitted.entries.takeLast(n = kept.size) != kept) broken += "an entry was cut or reordered"
            val isOver = size > room && !isAlone
            val hasNoLine = fitted.dropped > 0 && fitted.entries.size != kept.size + 1
            if (isOver) broken += "more than the room was kept"
            if (hasNoLine) broken += "no line for what was dropped"

            dropped = fitted.dropped
        }

        return broken
    }

    @Test
    fun `whatever is found, what is kept fits, whole and in order, and what was dropped never comes back`() =
        runTest {
            val random = Random(seed = 20261114)
            val broken = (1..TURNS).flatMap { turn(random = random) }

            assertEquals(
                expected = emptyList(),
                actual = broken.distinct()
            )
        }

    private companion object {
        const val TURNS = 300
        const val REPLY_TOKENS = 100
    }
}
