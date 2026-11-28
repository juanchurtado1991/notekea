package app.notekea.cards.domain

import app.notekea.core.domain.figures.BackedFigures
import app.notekea.core.llm.domain.LlmEngine

/**
 * The checks a card the model writes must pass against its sentence, whether made new or written anew: every figure
 * of its answer is the sentence's, and the reranker finds the sentence backs it, as 3.5 checks answers.
 */
class CardChecks(private val llmEngine: LlmEngine) {
    /** Every figure of [card]'s answer is its sentence's, or one worked from them. */
    fun isFigured(card: ProposedCard): Boolean = BackedFigures
        .missingIn(
            text = card.answer,
            sources = listOf(card.sentence)
        )
        .isEmpty()

    suspend fun isBacked(card: ProposedCard): Boolean {
        val score = llmEngine.rerank(
            query = "${card.question} ${card.answer}",
            texts = listOf(card.sentence)
        )

        val best = score.valueOrNull()?.firstOrNull() ?: Float.NEGATIVE_INFINITY

        return best >= SUPPORT
    }

    private companion object {
        const val SUPPORT = -3f
    }
}
