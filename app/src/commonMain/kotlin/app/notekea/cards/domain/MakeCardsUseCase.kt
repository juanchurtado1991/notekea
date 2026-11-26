package app.notekea.cards.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.figures.BackedFigures
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.llm.domain.ReplyText
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.RelativePath

/**
 * Flashcards made from a note by the model (4.6), a long one part by part ([CardsParts]), each kept when its sentence
 * is in the note and the reranker finds it backs the card, as 3.5 checks answers, and the note has no such card.
 */
class MakeCardsUseCase(
    private val llmEngine: LlmEngine,
    private val reader: CardsReader,
    private val noteContentStore: NoteContentStore,
    private val noteSections: NoteSections
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

        val parts = CardsParts.of(
            text = text,
            sections = noteSections.of(body = text),
            max = CardsPrompt.MAX_NOTE_CHARS
        )

        val replies = parts.mapIndexed { index, part ->
            proposedOf(
                title = note.nameWithoutExtension,
                part = part,
                place = if (parts.size == 1) null else "part ${index + 1} of ${parts.size}"
            )
        }

        if (replies.all { it == null }) return Outcome.Failure(error = CardsProblem.MODEL_FAILED)

        val proposed = replies.filterNotNull().flatten()

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
                        asked = asked,
                        most = CardsPrompt.MAX_CARDS * parts.size
                    )
                )
            }
        )
    }

    // The cards the model proposes from one [part] of the note, at its [place] when the note has several; null
    // when its reply is not the cards' JSON.
    private suspend fun proposedOf(
        title: String,
        part: String,
        place: String?
    ): List<ProposedCard>? {
        val json = try {
            ReplyText.of(
                llmEngine = llmEngine,
                messages = CardsPrompt.messages(
                    title = title,
                    text = part,
                    place = place
                ),
                options = GenerationOptions(
                    maxTokens = MAX_TOKENS,
                    temperature = 0f,
                    jsonSchema = CardsPrompt.SCHEMA
                )
            )
        } catch (_: LlmException) {
            return null
        }

        return reader.read(json = json)
    }

    // The first check [card] fails, in the order they cost; a card kept adds its question to [asked].
    private suspend fun verdictOf(
        card: ProposedCard,
        text: String,
        known: Set<String>,
        asked: MutableSet<String>,
        most: Int
    ): CardVerdict {
        val question = card.question.lowercase()

        val verdict = when {
            card.question.isBlank() || card.answer.isBlank() -> CardVerdict.BLANK
            question in known -> CardVerdict.KNOWN
            !isQuoted(card = card, text = text) -> CardVerdict.UNQUOTED
            !isFigured(card = card) -> CardVerdict.UNFIGURED
            !isBacked(card = card) -> CardVerdict.UNBACKED
            question in asked || asked.size >= most -> CardVerdict.REPEATED
            else -> CardVerdict.KEPT
        }

        if (verdict == CardVerdict.KEPT) asked += question

        return verdict
    }

    // Every figure of its answer is its sentence's, or one worked from them.
    private fun isFigured(card: ProposedCard): Boolean = BackedFigures
        .missingIn(
            text = card.answer,
            sources = listOf(card.sentence)
        )
        .isEmpty()

    private fun isQuoted(
        card: ProposedCard,
        text: String
    ): Boolean = CardFormat.isQuoted(
        sentence = card.sentence,
        text = text
    )

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

    private companion object {
        const val MAX_TOKENS = 900
        const val SUPPORT = -3f
    }
}
