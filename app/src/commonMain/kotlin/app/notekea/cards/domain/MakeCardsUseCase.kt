package app.notekea.cards.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.RelativePath

/**
 * Flashcards made from a note by the model (4.6), each kept only when its sentence is in the note and the reranker
 * finds that sentence backs the question and its answer, as 3.5 checks answers; questions the note already has as
 * cards are left out. Returns the cards, or why none could be made.
 */
class MakeCardsUseCase(
    private val llmEngine: LlmEngine,
    private val reader: CardsReader,
    private val noteContentStore: NoteContentStore
) {
    suspend operator fun invoke(
        vaultPath: String,
        note: RelativePath
    ): Outcome<List<ProposedCard>, CardsProblem> {
        val text = noteContentStore.read(
            vaultPath = vaultPath,
            path = note
        )
            ?: return Outcome.Failure(error = CardsProblem.NOTE_UNREADABLE)

        val json = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = CardsPrompt.messages(
                        title = note.nameWithoutExtension,
                        text = text
                    ),
                    options = GenerationOptions(
                        maxTokens = MAX_TOKENS,
                        temperature = 0f,
                        jsonSchema = CardsPrompt.SCHEMA
                    )
                )
                .collect { json.append(it) }
        } catch (_: LlmException) {
            return Outcome.Failure(error = CardsProblem.MODEL_FAILED)
        }

        val proposed =
            reader.read(json = json.toString()) ?: return failed(CardsProblem.MODEL_FAILED)

        val known = CardFormat
            .cardsOf(
                note = note,
                text = text
            )
            .map { it.question.lowercase() }
            .toSet()

        val kept = proposed
            .filter {
                it.question.isNotBlank() &&
                    it.answer.isNotBlank() &&
                    it.question.lowercase() !in known
            }
            .filter {
                isBacked(
                    card = it,
                    text = text
                )
            }
            .distinctBy { it.question.lowercase() }
            .take(n = CardsPrompt.MAX_CARDS)

        if (kept.isEmpty()) return failed(problem = CardsProblem.NONE_BACKED)

        return Outcome.Success(value = kept)
    }

    private suspend fun isBacked(
        card: ProposedCard,
        text: String
    ): Boolean {
        val isQuoted = plain(text = card.sentence).let {
            it.length >= MIN_SENTENCE && it in plain(text = text)
        }

        if (!isQuoted) return false

        val score = llmEngine.rerank(
            query = "${card.question} ${card.answer}",
            texts = listOf(card.sentence)
        )

        val best = (score as? Outcome.Success)?.value?.firstOrNull() ?: Float.NEGATIVE_INFINITY

        return best >= SUPPORT
    }

    private fun failed(problem: CardsProblem): Outcome<List<ProposedCard>, CardsProblem> =
        Outcome.Failure(error = problem)

    private fun plain(text: String): String = text.lowercase().filter { it.isLetterOrDigit() }

    private companion object {
        const val MAX_TOKENS = 900
        const val MIN_SENTENCE = 12
        const val SUPPORT = -3f
    }
}
