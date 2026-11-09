package app.notekea.core.embedding.domain

import app.notekea.core.domain.Outcome

/**
 * Turns texts into vectors whose closeness is closeness in meaning, in any language the model knows (`ROADMAP.md`
 * 2.2): the index embeds a note's chunks and a search its question, with the same model. Its adapter runs a small
 * multilingual model bundled with the app, off the UI thread; a cancelled call stops between texts.
 */
interface TextEmbedder {
    /** Which model makes the vectors: vectors of different models are not comparable, so the index starts over. */
    val modelId: String

    /** How many numbers each vector has. */
    val dimensions: Int

    /** The most tokens a text may have, as the model counts them: a longer one is refused, never cut. */
    val maxTokens: Int

    /** One vector per text, in order, each of length 1, so their dot product is their cosine similarity. */
    suspend fun embed(
        texts: List<String>
    ): Outcome<List<FloatArray>, EmbeddingError>
}
