package app.notekea.ai.data

/**
 * Reads llama.cpp's rerank reply, a JSON array of `{document, index, score, tokens_evaluated}`, into the scores in the
 * order of the documents given: the reply may list them in another. A quote inside a document is escaped (`\"`), so
 * a field name is only found where it is one.
 */
object RerankScores {
    private val INDEX = Regex(pattern = """(?<!\\)"index"\s*:\s*(\d+)""")
    private val SCORE = Regex(pattern = """(?<!\\)"score"\s*:\s*(-?[0-9.]+(?:[eE][-+]?\d+)?)""")

    /** The [count] scores of [json], by the index of their document; a document missing from it scores -infinity. */
    fun parse(
        json: String,
        count: Int
    ): List<Float> {
        val scores = FloatArray(size = count) { Float.NEGATIVE_INFINITY }
        val indexes = INDEX.findAll(input = json).map { it.groupValues[1].toInt() }
        val values = SCORE.findAll(input = json).map { it.groupValues[1].toFloat() }

        indexes.zip(other = values).forEach { (index, score) ->
            if (index in 0 until count) scores[index] = score
        }

        return scores.toList()
    }
}
