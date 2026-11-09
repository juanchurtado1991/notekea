package app.notekea.core.notes.domain

import app.notekea.core.domain.Outcome

/**
 * The search index of a vault's notes: each note's chunks ([NoteChunk]) with their vectors, found by
 * meaning (nearest vectors) and by keywords (BM25), the two rankings merged. It lives in the app's data, never inside
 * the vault; a change is on disk when the call returns, so an interrupted run resumes without losing it.
 */
interface VectorIndex {
    /** Replaces what the index holds of [note] with its [chunks], one vector each, in order. */
    suspend fun put(
        vaultPath: String,
        note: RelativePath,
        chunks: List<NoteChunk>,
        vectors: List<FloatArray>
    ): Outcome<Unit, IndexError>

    suspend fun remove(
        vaultPath: String,
        note: RelativePath
    ): Outcome<Unit, IndexError>

    /** Forgets every note of the vault (Rebuild index). */
    suspend fun clear(vaultPath: String): Outcome<Unit, IndexError>

    /** The [limit] chunks closest in meaning to [vector] alone, best first: the related notes of a passage (2.4). */
    suspend fun nearest(
        vaultPath: String,
        vector: FloatArray,
        limit: Int
    ): Outcome<List<SearchHit>, IndexError>

    /**
     * The [limit] chunks closest to the question, by its [text] and its [vector] together, best first, among the notes
     * [only] names, or all of them when null.
     */
    suspend fun search(
        vaultPath: String,
        text: String,
        vector: FloatArray,
        limit: Int,
        only: Set<RelativePath>?
    ): Outcome<List<SearchHit>, IndexError>
}
