package app.notekea.autotest

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.TextEmbedder
import kotlinx.coroutines.yield
import kotlin.math.sqrt

/**
 * The embedder of a case when the bundled model is not there (the Auto-Test's JUnit runs in CI, where no test loads a
 * real model): a text's vector counts its words by their hash, scaled to length 1, so the index works as with the
 * model, only less wisely. Notekea QA, installed, has the model and uses it.
 */
class StandInTextEmbedder : TextEmbedder {
    override val modelId: String = MODEL_ID

    override val dimensions: Int = DIMENSIONS

    override val maxTokens: Int = MAX_WORDS

    override suspend fun embed(texts: List<String>): Outcome<List<FloatArray>, EmbeddingError> =
        Outcome.Success(
            value = texts.map { vectorOf(text = it) }
        )

    private suspend fun vectorOf(text: String): FloatArray {
        yield()
        val vector = FloatArray(size = DIMENSIONS)

        WORD.findAll(input = text.lowercase()).forEach { word ->
            vector[(word.value.hashCode() and Int.MAX_VALUE) % DIMENSIONS] += 1f
        }

        val length = sqrt(vector.sumOf { (it * it).toDouble() }).toFloat().takeIf {
            it > 0f
        } ?: 1f

        return FloatArray(size = DIMENSIONS) {
            vector[it] / length
        }
    }

    private companion object {
        const val MODEL_ID = "auto-test-stand-in"
        const val DIMENSIONS = 64
        const val MAX_WORDS = 510
        val WORD = Regex(pattern = "\\p{L}+|\\d+")
    }
}
