package app.notekea.cards.domain

import app.notekea.cards.data.GhostChoicesReader
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A card made a multiple choice question only when the model gives three good wrong answers (2026-11-13). */
class MakeChoicesUseCaseTest {
    private val engine = FakeLlmEngine()

    private val card = StudyCard(
        note = RelativePath(value = "Capitals.md"),
        line = 1,
        question = "Capital of Peru",
        answer = "Lima"
    )

    private val makeChoices = MakeChoicesUseCase(
        llmEngine = engine,
        reader = GhostChoicesReader()
    )

    @Test
    fun `too few wrong answers, a reply that is not JSON, a failure or no model give no choices`() =
        runTest {
            val replies = listOf(
                "{\"wrong\":[\"Quito\",\"Quito.\",\"LIMA\"]}",
                "Quito, Bogotá, Caracas"
            ).map { reply ->
                engine.reply = { reply }
                makeChoices(card = card)
            }

            engine.failure = LlmError.EngineFailed
            val failed = makeChoices(card = card)

            engine.failure = null
            engine.readiness = Outcome.Failure(error = LlmError.NoModel)
            engine.generated.clear()
            val noModel = makeChoices(card = card)

            assertEquals(
                expected = listOf<Any?>(null, null, null, null, 0),
                actual = replies + failed + noModel + engine.generated.size
            )
        }
}
