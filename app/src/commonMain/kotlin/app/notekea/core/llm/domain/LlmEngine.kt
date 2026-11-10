package app.notekea.core.llm.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * The local chat model and its reranker. The model loads on the first call and is let go after some minutes without
 * one; replies run side by side up to the slots the machine allows. Platform implementations live outside
 * `commonMain` (llama.cpp on desktop).
 */
interface LlmEngine {
    /**
     * The reply to [messages], piece by piece as it is written, as [options] say. Cancelling the collector stops the
     * model; a reply that cannot be written fails with [LlmException].
     */
    fun generate(
        messages: List<ChatMessage>,
        options: GenerationOptions = GenerationOptions()
    ): Flow<String>

    /** How well each of [texts] answers [query], in order: the reranker's scores, higher is better. */
    suspend fun rerank(
        query: String,
        texts: List<String>
    ): Outcome<List<Float>, LlmError>
}
