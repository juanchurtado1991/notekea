package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.TextEmbedder

/**
 * The notes closest in meaning to the open note (`ROADMAP.md` 2.4), from its [text] as the editor has it: each of its
 * chunks is embedded as the index embeds them and its nearest chunks found ([VectorIndex.nearest]); a note scores its
 * closest chunk, the open note and those it already links are left out, and the [LIMIT] best are kept, each with the
 * passage that matched ([RelatedSnippet]). The notes it links come apart, for "Already linked".
 */
class FindRelatedNotesUseCase(
    private val chunkNote: ChunkNoteUseCase,
    private val textEmbedder: TextEmbedder,
    private val vectorIndex: VectorIndex,
    private val noteContentStore: NoteContentStore,
    private val linkFinder: LinkFinder
) {
    suspend operator fun invoke(
        vaultPath: String,
        note: RelativePath,
        text: String,
        notes: Collection<RelativePath>
    ): Outcome<RelatedNotes, IndexRunError> {
        val linked = linkedNotes(note = note, text = text, notes = notes)
        val chunks = chunkNote(note = note, text = text)
        if (chunks.isEmpty()) return Outcome.Success(value = RelatedNotes(related = emptyList(), linked = linked))
        val texts = chunks.map { chunk -> embeddingText(note = note, chunk = chunk) }
        val vectors = (textEmbedder.embed(texts = texts) as? Outcome.Success)?.value
            ?: return Outcome.Failure(error = IndexRunError.Model)
        val best = closest(vaultPath = vaultPath, vectors = vectors, leftOut = linked + note)
            ?: return Outcome.Failure(error = IndexRunError.Index)
        val words = WORD.findAll(input = text.lowercase()).map { it.value }.toSet()
        val related = best.take(n = LIMIT).mapNotNull { relatedOf(vaultPath = vaultPath, hit = it, words = words) }
        return Outcome.Success(value = RelatedNotes(related = related, linked = linked))
    }

    /** The closest chunk of each note but [leftOut], closest first; null when the index cannot be read. */
    private suspend fun closest(
        vaultPath: String,
        vectors: List<FloatArray>,
        leftOut: List<RelativePath>
    ): List<SearchHit>? {
        val best = HashMap<RelativePath, SearchHit>()
        vectors.forEach { vector ->
            val found = vectorIndex.nearest(vaultPath = vaultPath, vector = vector, limit = CANDIDATES)
            val hits = (found as? Outcome.Success)?.value ?: return null
            hits.filter { it.note !in leftOut }.forEach { hit ->
                if ((best[hit.note]?.score ?: Float.NEGATIVE_INFINITY) < hit.score) best[hit.note] = hit
            }
        }
        return best.values.sortedByDescending { it.score }
    }

    /** The notes [text] links to, as [LinkResolver] resolves them among [notes], once each, in order. */
    private fun linkedNotes(
        note: RelativePath,
        text: String,
        notes: Collection<RelativePath>
    ): List<RelativePath> {
        val resolver = LinkResolver(notePaths = notes)
        return linkFinder.find(markdown = text).mapNotNull { resolver.resolve(link = it, source = note)?.target }
            .filter { it != note }
            .distinct()
    }

    /** The text the model embeds for [chunk], as the index embeds the notes' chunks: title and headings in front. */
    private fun embeddingText(
        note: RelativePath,
        chunk: NoteChunk
    ): String = (listOf(note.nameWithoutExtension) + chunk.headingPath).joinToString(separator = HEADING_JOIN) +
        "\n" + chunk.text

    /** [hit]'s note with the passage that matched, read from its body; null when it can no longer be read. */
    private suspend fun relatedOf(
        vaultPath: String,
        hit: SearchHit,
        words: Set<String>
    ): RelatedNote? {
        val stored = noteContentStore.read(vaultPath = vaultPath, path = hit.note) ?: return null
        val body = NoteDocument.fromText(text = stored).body
        if (hit.range.last >= body.length) return null
        val snippet = RelatedSnippet.of(passage = body.substring(range = hit.range), words = words)
        return RelatedNote(path = hit.note, score = hit.score, snippet = snippet)
    }

    companion object {
        /** How many related notes the panel shows. */
        const val LIMIT = 8

        // The nearest chunks of each of the open note's chunks: enough for eight notes after the linked ones go.
        private const val CANDIDATES = 30
        private const val HEADING_JOIN = " › "
        private val WORD = Regex(pattern = "\\p{L}{3,}")
    }
}
