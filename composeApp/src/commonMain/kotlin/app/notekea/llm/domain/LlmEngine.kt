package app.notekea.llm.domain

import kotlinx.coroutines.flow.Flow

/**
 * Local language model used for both embeddings (indexing and retrieval) and chat generation.
 * Platform implementations live outside `commonMain` (llama.cpp on desktop).
 */
interface LlmEngine {
    suspend fun embed(
        text: String
    ): FloatArray

    fun generate(
        prompt: String
    ): Flow<String>
}
