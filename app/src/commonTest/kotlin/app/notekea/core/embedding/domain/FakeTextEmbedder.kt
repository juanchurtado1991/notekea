package app.notekea.core.embedding.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.math.sqrt

/**
 * [TextEmbedder] without a model: a text's vector counts its words, each adding to one of [DIMENSIONS] places (its
 * hash), scaled to length 1, so texts sharing words are close. It suspends between texts, for [millisPerText] if set.
 * [embedded] lists every text embedded; [failure] makes every call fail; a text over [maxTokens] words is refused.
 */
class FakeTextEmbedder(
    override val modelId: String = "fake-model",
    override val maxTokens: Int = MAX_WORDS
) : TextEmbedder {
    val embedded = mutableListOf<String>()
    var failure: EmbeddingError? = null

    /** How long each text takes, as the real model takes milliseconds; none by default. */
    var millisPerText = 0L

    override val dimensions: Int = DIMENSIONS

    override suspend fun embed(texts: List<String>): Outcome<List<FloatArray>, EmbeddingError> {
        failure?.let {
            return Outcome.Failure(error = it)
        }

        val vectors = texts.mapIndexed { index, text ->
            if (millisPerText > 0) delay(timeMillis = millisPerText) else yield()

            val words = WORD
                .findAll(input = text.lowercase())
                .map { it.value }
                .toList()

            if (words.size > maxTokens) {
                return Outcome.Failure(
                    error = EmbeddingError.TextTooLong(
                        index = index,
                        tokens = words.size
                    )
                )
            }

            embedded += text
            vectorOf(words = words)
        }

        return Outcome.Success(value = vectors)
    }

    private fun vectorOf(words: List<String>): FloatArray {
        val vector = FloatArray(size = DIMENSIONS)

        words.forEach { vector[(it.hashCode() and Int.MAX_VALUE) % DIMENSIONS] += 1f }

        val length = sqrt(vector.sumOf { (it * it).toDouble() }).toFloat().takeIf {
            it > 0f
        } ?: 1f

        return FloatArray(size = DIMENSIONS) {
            vector[it] / length
        }
    }

    private companion object {
        const val DIMENSIONS = 64
        const val MAX_WORDS = 400
        val WORD = Regex(pattern = "\\p{L}+|\\d+")
    }
}
