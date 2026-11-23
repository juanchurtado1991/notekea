package app.notekea.chat.eval

import app.notekea.ai.domain.ModelSize

/**
 * The model files the evaluations run on, by size, in the folder Gradle passes as `notekea.chat.models`. One place,
 * so a model changed for a measurement is changed for every evaluation; slots, the default size and the GPU stay in
 * each test, as they change its scores.
 */
object EvalModels {
    const val RERANKER_FILE = "bge-reranker-v2-m3-Q4_K_M.gguf"

    val FILES = mapOf(
        ModelSize.SMALL to "Qwen3.5-2B-Q4_K_M.gguf",
        ModelSize.MEDIUM to "Qwen3.5-4B-Q4_K_M.gguf",
        ModelSize.LARGE to "Qwen3.5-9B-Q4_K_M.gguf"
    )
}
