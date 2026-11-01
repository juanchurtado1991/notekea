package app.notekea.core.embedding.domain

/** Why texts could not be embedded; never what they say (`GUIDELINES.md` §13). */
sealed interface EmbeddingError {
    /** The model file is not where the app was installed (a broken installation). */
    data class ModelMissing(val path: String) : EmbeddingError

    /** The text at [index] has [tokens], more than [TextEmbedder.maxTokens]. */
    data class TextTooLong(
        val index: Int,
        val tokens: Int
    ) : EmbeddingError

    /** The engine failed to load the model or to embed (out of memory, an unsupported processor). */
    data object EngineFailed : EmbeddingError
}
