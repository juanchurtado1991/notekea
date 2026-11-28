package app.notekea.cards.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.llm.domain.ReplyText

/**
 * A hard card written anew from the note's sentence it comes from (4.6, plan step 4): the model writes other wordings
 * ([RewritePrompt]), and the first that asks something new and passes a new card's checks ([CardChecks]) is proposed.
 * Null when the card has no sentence, the note no longer writes it, there is no model, or no wording passes.
 */
class RewriteCardUseCase(
    private val llmEngine: LlmEngine,
    private val reader: CardsReader
) {
    private val checks = CardChecks(llmEngine = llmEngine)

    suspend operator fun invoke(card: StudyCard): ProposedCard? {
        val sentence = card.source
        if (sentence == null || card.isOutdated) return null
        if (llmEngine.readiness() is Outcome.Failure) return null

        val json = try {
            ReplyText.of(
                llmEngine = llmEngine,
                messages = RewritePrompt.messages(
                    sentence = sentence,
                    card = card
                ),
                options = GenerationOptions(
                    maxTokens = MAX_TOKENS,
                    temperature = TEMPERATURE,
                    jsonSchema = RewritePrompt.SCHEMA
                )
            )
        } catch (_: LlmException) {
            return null
        }

        val old = keyOf(text = card.question)

        return reader
            .read(json = json)
            .orEmpty()
            .map { it.copy(sentence = sentence) }
            .filter {
                it.question.isNotBlank() &&
                    it.answer.isNotBlank() &&
                    keyOf(text = it.question) != old
            }
            .firstOrNull {
                checks.isFigured(card = it) && checks.isBacked(card = it)
            }
    }

    // Two questions are the same when only their case, spaces or punctuation differ.
    private fun keyOf(text: String): String = text.lowercase().filter { it.isLetterOrDigit() }

    private companion object {
        const val MAX_TOKENS = 400
        const val TEMPERATURE = 0.7f
    }
}
