package app.notekea.cards.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.llm.domain.ReplyText
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
        val judged = when (
            val made = judged(
                vaultPath = vaultPath,
                note = note
            )
        ) {
            is Outcome.Success -> made.value
            is Outcome.Failure -> return Outcome.Failure(error = made.error)
        }

        val kept = judged
            .filter {
                it.verdict == CardVerdict.KEPT
            }
            .map { it.card }

        if (kept.isEmpty()) return failed(problem = CardsProblem.NONE_BACKED)

        return Outcome.Success(value = kept)
    }

    /** Every card the model proposed for [note], each with what became of it; or why the model gave none. */
    suspend fun judged(
        vaultPath: String,
        note: RelativePath
    ): Outcome<List<JudgedCard>, CardsProblem> {
        val text = noteContentStore.read(
            vaultPath = vaultPath,
            path = note
        )
            ?: return Outcome.Failure(error = CardsProblem.NOTE_UNREADABLE)

        val json = try {
            ReplyText.of(
                llmEngine = llmEngine,
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
        } catch (_: LlmException) {
            return Outcome.Failure(error = CardsProblem.MODEL_FAILED)
        }

        val proposed =
            reader.read(json = json) ?: return Outcome.Failure(error = CardsProblem.MODEL_FAILED)

        val known = CardFormat
            .cardsOf(
                note = note,
                text = text
            )
            .map { it.question.lowercase() }
            .toSet()

        val asked = mutableSetOf<String>()

        return Outcome.Success(
            value = proposed.map { card ->
                JudgedCard(
                    card = card,
                    verdict = verdictOf(
                        card = card,
                        text = text,
                        known = known,
                        asked = asked
                    )
                )
            }
        )
    }

    // The first check [card] fails, in the order they cost; a card kept adds its question to [asked].
    private suspend fun verdictOf(
        card: ProposedCard,
        text: String,
        known: Set<String>,
        asked: MutableSet<String>
    ): CardVerdict {
        val question = card.question.lowercase()

        val verdict = when {
            card.question.isBlank() || card.answer.isBlank() -> CardVerdict.BLANK
            question in known -> CardVerdict.KNOWN
            !isQuoted(card = card, text = text) -> CardVerdict.UNQUOTED
            !isBacked(card = card) -> CardVerdict.UNBACKED
            question in asked || asked.size >= CardsPrompt.MAX_CARDS -> CardVerdict.REPEATED
            else -> CardVerdict.KEPT
        }

        if (verdict == CardVerdict.KEPT) asked += question

        return verdict
    }

    private fun isQuoted(
        card: ProposedCard,
        text: String
    ): Boolean = plain(text = card.sentence).let {
        it.length >= MIN_SENTENCE && it in plain(text = text)
    }

    private suspend fun isBacked(card: ProposedCard): Boolean {
        val score = llmEngine.rerank(
            query = "${card.question} ${card.answer}",
            texts = listOf(card.sentence)
        )

        val best = score.valueOrNull()?.firstOrNull() ?: Float.NEGATIVE_INFINITY

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
