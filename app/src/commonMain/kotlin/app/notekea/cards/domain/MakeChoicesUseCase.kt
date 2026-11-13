package app.notekea.cards.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import kotlin.random.Random

/**
 * A card made a multiple choice question: three wrong answers the chat model writes ([ChoicesPrompt]), kept only
 * when they are three, different from each other and from the right answer, shuffled with it. Null when there is no
 * model or its reply is not that: the card is then asked as before, its answer shown on request.
 */
class MakeChoicesUseCase(
    private val llmEngine: LlmEngine,
    private val reader: ChoicesReader,
    private val random: Random = Random.Default
) {
    suspend operator fun invoke(card: StudyCard): CardChoices? {
        if (llmEngine.readiness() is Outcome.Failure) return null

        val json = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = ChoicesPrompt.messages(card = card),
                    options = GenerationOptions(
                        maxTokens = MAX_TOKENS,
                        temperature = TEMPERATURE,
                        jsonSchema = ChoicesPrompt.SCHEMA
                    )
                )
                .collect { json.append(it) }
        } catch (_: LlmException) {
            return null
        }

        val rightKey = keyOf(text = card.answer)

        val wrong = reader
            .read(json = json.toString())
            .orEmpty()
            .map { it.trim() }
            .filter {
                it.isNotEmpty() && keyOf(text = it) != rightKey
            }
            .distinctBy { keyOf(text = it) }

        if (wrong.size < ChoicesPrompt.WRONG_ANSWERS) return null

        val right = card.answer.trim()

        val options = wrong
            .take(n = ChoicesPrompt.WRONG_ANSWERS)
            .plus(element = right)
            .shuffled(random = random)

        return CardChoices(
            options = options,
            correct = options.indexOf(element = right)
        )
    }

    // Two answers are the same when only their case, spaces or punctuation differ.
    private fun keyOf(text: String): String = text.lowercase().filter { it.isLetterOrDigit() }

    private companion object {
        const val MAX_TOKENS = 200
        const val TEMPERATURE = 0.7f
    }
}
