package app.notekea.chat.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.SearchHit
import app.notekea.core.notes.domain.VectorIndex
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Answers a question from the notes in one call, as the 3.0 spikes measured best: the index's closest passages are
 * reranked, nothing is answered when even the best scores below [GATE], and the model writes from the five best,
 * citing them. The steps come as [AnswerStep]s, the answer's text as it is written.
 */
class AnswerFromNotesUseCase(
    private val textEmbedder: TextEmbedder,
    private val vectorIndex: VectorIndex,
    private val noteTextCache: NoteTextCache,
    private val llmEngine: LlmEngine
) {
    /**
     * The answer to [question] from [notes] of the vault at [vaultPath], only from the notes [only] names if given;
     * with more than one of [drafts] (Deep answer, 3.6), several are written and voted on ([DraftVote]).
     */
    operator fun invoke(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        question: String,
        only: Set<RelativePath>? = null,
        drafts: Int = 1
    ): Flow<AnswerStep> = flow {
        emit(value = AnswerStep.Searching)

        val found = when (
            val passages = passagesFor(
                vaultPath = vaultPath,
                notes = notes,
                question = question,
                only = only
            )
        ) {
            is Outcome.Success -> passages.value
            is Outcome.Failure -> return@flow emit(value = failed(error = passages.error))
        }

        if (found.isEmpty()) return@flow emit(value = AnswerStep.NotFound)
        emit(value = AnswerStep.Reading(notes = found.distinctBy { it.note }.size))

        val read = when (val best = best(question = question, passages = found)) {
            is Outcome.Success -> best.value
            is Outcome.Failure -> return@flow emit(value = failed(error = best.error))
        }

        if (read.isEmpty()) return@flow emit(value = AnswerStep.NotFound)

        val answer = if (drafts > 1) {
            voted(
                question = question,
                read = read,
                drafts = drafts
            )
        } else {
            written(
                question = question,
                read = read
            )
        }

        emitAll(flow = answer)
    }

    /** The answer written once, shown as it is written. */
    private fun written(
        question: String,
        read: List<AnswerSource>
    ): Flow<AnswerStep> = flow {
        val written = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = AnswerPrompt.messages(
                        question = question,
                        excerpts = read.map { it.text }
                    ),
                    options = GenerationOptions(maxTokens = MAX_TOKENS)
                )
                .collect { piece ->
                    written.append(piece)
                    val isShown = !AnswerPrompt.NOT_FOUND.startsWith(prefix = written.trim())

                    if (isShown) {
                        emit(
                            value = AnswerStep.Writing(
                                text = written.toString().trim(),
                                read = read
                            )
                        )
                    }
                }
        } catch (exception: LlmException) {
            return@flow emit(value = failed(error = ChatError.Model(error = exception.error)))
        }

        val text = written.toString().trim()
        val isNotFound = text.isEmpty() || AnswerPrompt.NOT_FOUND in text
        if (isNotFound) return@flow emit(value = AnswerStep.NotFound)

        emit(
            value = AnswerStep.Done(
                answer = AnswerCitations.of(
                    text = text,
                    read = read
                )
            )
        )
    }

    /** [drafts] answers written at once with different seeds, the one [DraftVote] chooses shown when all are done. */
    private fun voted(
        question: String,
        read: List<AnswerSource>,
        drafts: Int
    ): Flow<AnswerStep> = flow {
        emit(value = AnswerStep.Drafting(drafts = drafts))

        val texts = try {
            coroutineScope {
                (1..drafts)
                    .map { seed ->
                        async {
                            draftOf(
                                question = question,
                                read = read,
                                seed = seed
                            )
                        }
                    }
                    .awaitAll()
            }
        } catch (exception: LlmException) {
            return@flow emit(value = failed(error = ChatError.Model(error = exception.error)))
        }

        val winner =
            DraftVote.winner(drafts = texts) ?: return@flow emit(value = AnswerStep.NotFound)

        emit(
            value = AnswerStep.Done(
                answer = AnswerCitations.of(
                    text = winner.trim(),
                    read = read
                )
            )
        )
    }

    private suspend fun draftOf(
        question: String,
        read: List<AnswerSource>,
        seed: Int
    ): String {
        val draft = StringBuilder()

        llmEngine
            .generate(
                messages = AnswerPrompt.messages(
                    question = question,
                    excerpts = read.map { it.text }
                ),
                options = GenerationOptions(
                    maxTokens = MAX_TOKENS,
                    temperature = DRAFT_TEMPERATURE,
                    seed = seed
                )
            )
            .collect { draft.append(it) }

        return draft.toString()
    }

    /** The index's [CANDIDATES] closest passages to [question], with their text. */
    private suspend fun passagesFor(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        question: String,
        only: Set<RelativePath>?
    ): Outcome<List<AnswerSource>, ChatError> {
        val vector =
            (textEmbedder.embed(texts = listOf(question)) as? Outcome.Success)?.value?.single()
                ?: return Outcome.Failure(error = ChatError.Search)

        val hits = (
            vectorIndex.search(
                vaultPath,
                question,
                vector,
                CANDIDATES,
                only
            ) as? Outcome.Success
            )?.value
            ?: return Outcome.Failure(error = ChatError.Search)

        val byPath = notes.associateBy { it.file.path }

        val texts = noteTextCache.textsOf(
            vaultPath = vaultPath,
            notes = hits.mapNotNull {
                byPath[it.note]
            }.distinct()
        )

        return Outcome.Success(
            value = hits.mapNotNull { hit ->
                sourceOf(
                    hit = hit,
                    body = texts[hit.note]?.body
                )
            }
        )
    }

    private fun sourceOf(
        hit: SearchHit,
        body: String?
    ): AnswerSource? {
        val isInBody = body != null && hit.range.last < body.length
        if (!isInBody) return null

        return AnswerSource(
            number = 0,
            note = hit.note,
            headingPath = hit.headingPath,
            range = hit.range,
            text = body.substring(range = hit.range)
        )
    }

    /** The [EXCERPTS] passages the reranker scores best, numbered from 1; none when even the best is under [GATE]. */
    private suspend fun best(
        question: String,
        passages: List<AnswerSource>
    ): Outcome<List<AnswerSource>, ChatError> {
        val scores = when (
            val reranked = llmEngine.rerank(query = question, texts = passages.map { it.text })
        ) {
            is Outcome.Success -> reranked.value

            is Outcome.Failure -> return Outcome.Failure(
                error = ChatError.Model(error = reranked.error)
            )
        }

        val ranked = passages
            .zip(other = scores)
            .sortedByDescending { it.second }

        val isAnswerable = (ranked.firstOrNull()?.second ?: Float.NEGATIVE_INFINITY) >= GATE

        val best = ranked
            .take(n = if (isAnswerable) EXCERPTS else 0)
            .mapIndexed { index, (source, _) -> source.copy(number = index + 1) }

        return Outcome.Success(value = best)
    }

    private fun failed(error: ChatError): AnswerStep = AnswerStep.Failed(error = error)

    companion object {
        /** Under this best reranker score the notes do not answer: −5 turned away answerable questions (3.0). */
        const val GATE = -8f

        private const val CANDIDATES = 20
        private const val EXCERPTS = 5
        private const val MAX_TOKENS = 200

        // Drafts must differ to be worth a vote (docs/spikes/2026-11-08-answer-quality.md, "Vote").
        private const val DRAFT_TEMPERATURE = 0.7f
    }
}
