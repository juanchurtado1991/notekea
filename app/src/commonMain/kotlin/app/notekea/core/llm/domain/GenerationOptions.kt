package app.notekea.core.llm.domain

/**
 * How a reply is written: at most [maxTokens], with [temperature] and [seed] (the same seed and messages give the same
 * reply), and shaped by [jsonSchema] when a step needs JSON (a grammar keeps every reply valid).
 */
data class GenerationOptions(
    val maxTokens: Int = DEFAULT_MAX_TOKENS,
    val temperature: Float = DEFAULT_TEMPERATURE,
    val seed: Int = DEFAULT_SEED,
    val jsonSchema: String? = null
) {
    companion object {
        const val DEFAULT_MAX_TOKENS = 256
        const val DEFAULT_SEED = 42

        // Low: answers from notes should say what the notes say (docs/spikes/2026-11-08-answer-quality.md).
        const val DEFAULT_TEMPERATURE = 0.2f
    }
}
