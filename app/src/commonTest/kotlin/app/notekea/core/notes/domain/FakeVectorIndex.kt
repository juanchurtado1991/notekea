package app.notekea.core.notes.domain

import app.notekea.core.domain.Outcome

/**
 * [VectorIndex] in memory, per vault: each note's chunks and vectors, found by the dot product of the vectors and by
 * the question's words in the text, the two merged by [RankFusion] as the real index does. [isBusy] makes every change
 * fail as an index another Notekea holds; [puts] lists every note put, in order.
 */
class FakeVectorIndex : VectorIndex {
    val notes = mutableMapOf<String, MutableMap<RelativePath, List<Pair<NoteChunk, FloatArray>>>>()
    val puts = mutableListOf<RelativePath>()
    var isBusy = false

    override suspend fun put(
        vaultPath: String,
        note: RelativePath,
        chunks: List<NoteChunk>,
        vectors: List<FloatArray>
    ): Outcome<Unit, IndexError> = change {
        puts += note
        notes.getOrPut(key = vaultPath) { mutableMapOf() }[note] = chunks.zip(other = vectors)
    }

    override suspend fun remove(
        vaultPath: String,
        note: RelativePath
    ): Outcome<Unit, IndexError> = change { notes[vaultPath]?.remove(key = note) }

    override suspend fun clear(
        vaultPath: String
    ): Outcome<Unit, IndexError> = change { notes.remove(key = vaultPath) }

    override suspend fun nearest(
        vaultPath: String,
        vector: FloatArray,
        limit: Int
    ): Outcome<List<SearchHit>, IndexError> {
        val scored = notes[vaultPath].orEmpty().values.flatten().map { (chunk, v) -> chunk to dot(v, vector) }
        val best = scored.sortedByDescending { it.second }.take(n = limit)
        return Outcome.Success(value = best.map { (chunk, score) -> hitOf(chunk, score) })
    }

    override suspend fun search(
        vaultPath: String,
        text: String,
        vector: FloatArray,
        limit: Int,
        only: Set<RelativePath>?
    ): Outcome<List<SearchHit>, IndexError> {
        val chunks = notes[vaultPath].orEmpty().filterKeys { only == null || it in only }.values.flatten()
        val byMeaning = chunks.sortedByDescending { (_, v) -> v.indices.sumOf { (v[it] * vector[it]).toDouble() } }
        val words = text.lowercase().split(' ').filter { it.isNotBlank() }
        val byWords = chunks.filter { (chunk, _) -> words.any { it in chunk.text.lowercase() } }
        val hits = RankFusion.merge(rankings = listOf(byMeaning.map { it.first }, byWords.map { it.first }))
        return Outcome.Success(
            value = hits.take(n = limit).map { (chunk, score) ->
                val closeness = (1f + dot(chunks.first { it.first == chunk }.second, vector)) / 2f
                val hasEvery = words.all { it in chunk.text.lowercase() }
                hitOf(chunk, score).copy(hasWords = hasEvery, closeness = closeness)
            }
        )
    }

    private fun dot(
        first: FloatArray,
        second: FloatArray
    ): Float = first.indices.sumOf { (first[it] * second[it]).toDouble() }.toFloat()

    private fun hitOf(
        chunk: NoteChunk,
        score: Float
    ) = SearchHit(
        note = chunk.note,
        index = chunk.index,
        headingPath = chunk.headingPath,
        range = chunk.range,
        score = score
    )

    private fun change(
        block: () -> Unit
    ): Outcome<Unit, IndexError> {
        if (isBusy) return Outcome.Failure(error = IndexError.Busy)
        block()
        return Outcome.Success(value = Unit)
    }
}
