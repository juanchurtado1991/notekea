package app.notekea.index.domain

/**
 * Hybrid (vector + keyword) index over note chunks.
 * Platform implementations live outside `commonMain` (Lucene on desktop).
 */
interface VectorIndex {
    suspend fun remove(
        notePath: String
    )

    suspend fun search(
        query: String,
        embedding: FloatArray,
        limit: Int
    ): List<String>

    suspend fun upsert(
        notePath: String,
        chunks: List<String>,
        embeddings: List<FloatArray>
    )
}
