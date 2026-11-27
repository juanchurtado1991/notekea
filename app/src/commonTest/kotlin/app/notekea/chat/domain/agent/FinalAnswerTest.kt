package app.notekea.chat.domain.agent

import app.notekea.chat.domain.context.ContextBudget
import app.notekea.core.llm.domain.FakeLlmEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What the answer is written from (the bug hunt of 2026-11-17, L7): every entry that began with "(" was dropped,
 * also what the budget kept aside of the dropped results and a result shown again, so the answer had nothing.
 */
class FinalAnswerTest {
    private val engine = FakeLlmEngine()
    private val finalAnswer = AgentFinalAnswer(llmEngine = engine)

    @Test
    fun `what the budget kept aside and a result shown again are answered from, the loop's own lines are not`() =
        runTest {
            engine.reply = { "The rent is 950 euros." }

            val kept = ContextBudget.RECALLED + "From step 1, read Lease:\nThe rent is 950 euros."
            val again = AgentFinalAnswer.SHOWN_AGAIN + "read Lease:\nThe rent is 950 euros."
            val above = "(read Lease again: its result is above.)"

            val answers = listOf(kept, again, above).map {
                finalAnswer.written(
                    task = "how much is the rent?",
                    found = listOf(it),
                    summary = SUMMARY,
                    memory = emptyList(),
                    onText = {}
                )
            }

            assertEquals(
                expected = listOf("The rent is 950 euros.", "The rent is 950 euros.", SUMMARY),
                actual = answers
            )
        }

    private companion object {
        const val SUMMARY = "The summary."
    }
}
