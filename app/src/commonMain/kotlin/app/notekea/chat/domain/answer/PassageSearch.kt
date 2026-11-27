package app.notekea.chat.domain.answer

import app.notekea.chat.domain.ChatError
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.WebSourcesFolder
import app.notekea.core.notes.domain.index.SearchHit
import app.notekea.core.notes.domain.index.VectorIndex

/**
 * The passages of the notes that answer a query, as the 3.0 spikes measured best: the index's closest passages
 * reranked, none when even the best scores below [GATE]. What the loop's search and its frame read.
 */
class PassageSearch(
    private val textEmbedder: TextEmbedder,
    private val vectorIndex: VectorIndex,
    private val noteTextCache: NoteTextCache,
    private val llmEngine: LlmEngine
) {
    /**
     * The passages the reranker scores best for [query] (none under [GATE]), numbered from 1.
     */
    suspend fun excerpts(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        query: String
    ): Outcome<List<AnswerSource>, ChatError> {
        val found = when (val passages = passagesFor(vaultPath, notes, query)) {
            is Outcome.Success -> passages.value
            is Outcome.Failure -> return passages
        }

        return if (found.isEmpty()) {
            Outcome.Success(value = emptyList())
        } else {
            best(
                question = query,
                passages = found
            )
        }
    }

    /** The index's [CANDIDATES] closest passages to [question], with their text. */
    private suspend fun passagesFor(
        vaultPath: String,
        notes: Collection<NoteEntry>,
        question: String
    ): Outcome<List<AnswerSource>, ChatError> {
        val vector = textEmbedder
            .embed(texts = listOf(question))
            .valueOrNull()
            ?.single()
            ?: return Outcome.Failure(error = ChatError.Search)

        val hits = vectorIndex.search(
            vaultPath,
            question,
            vector,
            CANDIDATES,
            null
        ).valueOrNull()
            ?: return Outcome.Failure(error = ChatError.Search)

        val byPath = notes.associateBy { it.file.path }

        val texts = noteTextCache.textsOf(
            vaultPath = vaultPath,
            notes = hits.mapNotNull {
                byPath[it.note]
            }.distinct()
        )

        val passages = hits.mapNotNull { hit ->
            sourceOf(
                hit = hit,
                body = texts[hit.note]?.body
            )
        }

        // The frontmatter is not in the index: the properties of the notes the question names compete beside it.
        return Outcome.Success(value = passages + PropertyPassages.of(question, notes))
    }

    private fun sourceOf(
        hit: SearchHit,
        body: String?
    ): AnswerSource? {
        val isInBody = body != null && hit.range.last < body.length
        if (!isInBody) return null

        // A clipped page is cited from its own text, never from the notes a model wrote about it above: a citation
        // must hold what the source says, not a summary that may be wrong (the developer, 2026-11-14).
        val original = WebSourcesFolder.originalStartIn(body = body)
        val isSummary = original != null && hit.range.first < original
        if (isSummary) return null

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

    companion object {
        /** Under this best reranker score the notes do not answer: −5 turned away answerable questions (3.0). */
        const val GATE = -8f

        private const val CANDIDATES = 20
        private const val EXCERPTS = 5
    }
}
